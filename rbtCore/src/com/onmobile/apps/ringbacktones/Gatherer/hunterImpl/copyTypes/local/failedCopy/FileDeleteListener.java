package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.failedCopy;

import java.io.File;

public class FileDeleteListener
{
    private String fileName = null;

    public FileDeleteListener(String fileName)
    {
        super();
        this.fileName = fileName;
    }

    private boolean fileReadCompleted = false;
    private int totalFileCount = 0;
    private int recWrittenToDB = 0;

    public boolean isFileReadCompleted()
    {
        return fileReadCompleted;
    }

    public void setFileReadCompleted(boolean fileReadCompleted)
    {
        this.fileReadCompleted = fileReadCompleted;
    }

    public void incrementTotal()
    {
        totalFileCount++;
    }

    public void deleteFileIfNeeded()
    {
        recWrittenToDB++;
        if (isFileReadCompleted())
        {
            if (totalFileCount >= recWrittenToDB)
            {
                new File(fileName).delete();
            }
        }

    }
}
