package de.lmu.bio.calcium;

import java.util.ArrayList;

public class CaTask extends Thread  {

    protected boolean success;
    protected Exception error;

    ArrayList<Observer> observerList = new ArrayList<Observer>();

    public CaTask(String name) {
        super(name);
    }

    public void addObserver(Observer observer) {
        observerList.add(observer);
    }

    public void removeObserver(Observer observer) {
        observerList.remove(observer);
    }

    protected void fireTaskFinished(boolean success, Exception e) {
        for (Observer observer : observerList) {
            observer.taskFinished(success, e);
        }
    }

    protected void fireTaskProgress(int itemsProcessed, int ofTotalItems, String message) {
        for (Observer observer : observerList) {
            observer.taskProgress(itemsProcessed, ofTotalItems, message);
        }
    }
    protected void runTask() throws Exception {

    }

    @Override
    public void run() {

        success = true;
        error = null;

        try {
            runTask();
        } catch (Exception e) {
            error = e;
            e.printStackTrace();
            success = false;
        }

        fireTaskFinished(success, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error != null ? error.getMessage() : "internal error";
    }

    public interface Observer {
        public void taskProgress(int itemsProcessed, int ofTotalItems, String message);
        public void taskFinished(boolean success, Exception e);

    }

}
