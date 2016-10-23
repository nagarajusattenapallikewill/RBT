package com.onmobile.apps.ringbacktones.utils;

import java.util.LinkedList;

public class GeneralQueue
{
    private LinkedList m_Queue = null;

    public GeneralQueue()
    {
        m_Queue = new LinkedList();
    }

    public synchronized void enQueue(Object o)
    {
        m_Queue.addLast(o);
    }

    public synchronized Object deQueue()
    {
        return m_Queue.removeFirst();
    }

    public synchronized int size()
    {
        return m_Queue.size();
    }

    public synchronized boolean isEmpty()
    {
        return m_Queue.isEmpty();
    }
}