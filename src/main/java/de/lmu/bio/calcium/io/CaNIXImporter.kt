package de.lmu.bio.calcium.io

import de.lmu.bio.calcium.CaTask
import de.lmu.bio.calcium.model.CaImage
import de.lmu.bio.calcium.model.CaNeuron
import de.lmu.bio.calcium.model.CaRoiBox
import ij.IJ
import ij.gui.PolygonRoi
import ij.gui.Roi
import org.g_node.nix.*;

class CaNIXImporter(val path: String)
  : CaImporter("Importing") {

  var imagesTotal = 0
  var imagesProcessed = 0

  // importer state
  var theNeuron: CaNeuron? = null

  override fun runTask() {
    val fd = File.open(path, FileMode.ReadOnly)
    val block = fd.getBlocks{ b -> b.type == "neuron" }.lastOrNull() ?: throw RuntimeException("No neuron in file")

    theNeuron = CaNeuron(block.name)

    var imageGroups = block.getGroups { g -> g.type == "image.ca"}
    imagesTotal = imageGroups.count()

    imageGroups.map { g ->
      importImage(g)
    }.forEach { i ->
      theNeuron?.add(i)
    }

    block.close()
    fd.close()
  }

  fun importImage(parent: Group) : CaImage {
    fireTaskProgress(imagesProcessed, imagesTotal, "Importing " + parent.name)
    val meta = parent.metadata

    val filename = meta.getProperty("filename").values[0].string
    var ps = if (path.endsWith("/")) path else path + "/"

    var path = ps + filename
    if (! java.io.File(path).exists()) {
      path = meta.getProperty("original_path").values[0].string
    }

    var img = CaImage(path)
    var md = img.Metadata()

    //we should check that we actually have that info
    md.ctime = meta.getProperty("creation_time").values[0].long
    md.channels = meta.getProperty("channels").values[0].int

    val chans = parent.getDataArray(parent.name + ".channels")
    val data = IntArray(chans.dataExtent.data[0])
    val dim = chans.dimensions[0].asRangeDimension()
    val ticks = dim.ticks

    md.planeInfo = Array<CaImage.PlaneInfo>(data.size, { idx ->
      var info = img.PlaneInfo()
      info.timePoint = idx
      info.channel = data[idx]
      info.deltaT = ticks[idx]
      info
    })

    img.metadata = md

    var roiData = parent.getDataArrays {
      d -> d.type.startsWith("roi.pt", true)
    }

    roiData.map { d ->
      val shape = d.dataExtent
      val xy = FloatArray(shape.elementsProduct.toInt())
      d.getData(xy, shape, NDSize(2, 0))
      val roiMeta = d.metadata
      val roiType = roiMeta.getProperty("type").values[0].int
      val strokeWidth = roiMeta.getProperty("strokeWidth").values[0].double

      val x = xy.copyOfRange(0, shape.data[1])
      val y = xy.copyOfRange(shape.data[1], xy.size)
      var roi = PolygonRoi(x, y, roiType)
      roi.strokeWidth = strokeWidth.toFloat()
      roi.name = d.name.split(".").last().toUpperCase()

      CaRoiBox(roi)
    }.forEach { roi -> img.add(roi) }

    imagesProcessed++
    return img
  }

  override fun getNeuron(): CaNeuron? {
    return theNeuron
  }
}