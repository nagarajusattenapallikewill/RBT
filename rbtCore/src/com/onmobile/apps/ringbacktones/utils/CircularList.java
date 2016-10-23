package com.onmobile.apps.ringbacktones.utils;

import java.util.LinkedList;

public class CircularList
{
    private LinkedList listMembers;
    private int currentPointer;
    private boolean jumpedToFirstitemFlag;
    private boolean jumpedToLastItemFlag;

    public CircularList()
    {
        listMembers = new LinkedList();
        currentPointer = 0;
        jumpedToFirstitemFlag = false;
        jumpedToLastItemFlag = false;
    }

    /**
     * takes an object array as an argument and constructs the circular list in
     * that order
     * @param objectArray
     */
    public CircularList(Object[] objectArray)
    {
        listMembers = new LinkedList();
        for (int i = 0; i < objectArray.length; i++)
            listMembers.addLast(objectArray[i]);
        currentPointer = 0;
        jumpedToFirstitemFlag = false;
        jumpedToLastItemFlag = false;
    }

    /**
     * Adds the object to the top of list
     * @param object
     */
    public void addToHead(Object object)
    {
        listMembers.addFirst(object);
    }

    /**
     * adds the object to the bottom of the list
     * @param object
     */
    public void addToTail(Object object)
    {
        listMembers.addLast(object);
    }

    /**
     * adds the object to the specified position
     * @param object
     * @param position
     */
    public void add(Object object, int position)
    {
        listMembers.add(position, object);
    }

    /**
     * returns the Object in the position
     * @param position
     * @return Object
     */
    public Object get(int position)
    {
        return listMembers.get(position);
    }

    /**
     * returns the number of elements in this list
     * @return count
     */
    public int count()
    {
        return listMembers.size();
    }

    /**
     * decremetns current pointer
     */
    public void incrementCurrentPointer()
    {
        if (currentPointer == (listMembers.size() - 1))
        {
            currentPointer = 0;
            jumpedToFirstitemFlag = true;
        }
        else
            currentPointer++;
    }

    /**
     * decrements current pointer
     */
    public void decrementCurrentPointer()
    {
        if (currentPointer == 0)
        {
            currentPointer = listMembers.size() - 1;
            jumpedToLastItemFlag = true;
        }
        else
            currentPointer--;
    }

    /**
     * returns the object to which currentPointer is pointing to
     * @return object
     */
    public Object getCurrentObject()
    {
        return listMembers.get(currentPointer);
    }

    public void setCurrentPointer(int pointer)
    {
        currentPointer = pointer;
    }

    /**
     * Returns an array of objects
     * @return Object[]
     */
    public Object[] toArray()
    {
        Object[] objectArray = new Object[listMembers.size()];
        for (int i = 0; i < listMembers.size(); i++)
            objectArray[i] = listMembers.get(i);
        return objectArray;
    }

    /**
     * deletes all items from this list
     */
    public void clearList()
    {
        listMembers = new LinkedList();
    }

    /**
     * @return object Returns the jumpedToFirstitemFlag.
     */
    public boolean isJumpedToFirstitemFlag()
    {
        return jumpedToFirstitemFlag;
    }

    /**
     * @param jumpedToFirstitemFlag The jumpedToFirstitemFlag to set.
     */
    public void setJumpedToFirstitemFlag(boolean jumpedToFirstitemFlag)
    {
        this.jumpedToFirstitemFlag = jumpedToFirstitemFlag;
    }

    /**
     * @return boolean Returns the jumpedToLastItemFlag.
     */
    public boolean isJumpedToLastItemFlag()
    {
        return jumpedToLastItemFlag;
    }

    /**
     * @param jumpedToLastItemFlag The jumpedToLastItemFlag to set.
     */
    public void setJumpedToLastItemFlag(boolean jumpedToLastItemFlag)
    {
        this.jumpedToLastItemFlag = jumpedToLastItemFlag;
    }

    /**
     * clears the jumped to first item from last item flag
     */
    public void clearJumpedToFirstItemFlag()
    {
        this.jumpedToFirstitemFlag = false;
    }

    /**
     * clears jumped to the end of list flag
     */
    public void clearJumpedToLastItemFlag()
    {
        this.jumpedToLastItemFlag = false;
    }

    /**
     * @return Returns the currentPointer.
     */
    public int getCurrentPointer()
    {
        return currentPointer;
    }

    /**
     * prints the contents on screen
     */
    public void dumpOnScreen()
    {
        for (int index = 0; index < listMembers.size(); index++)
        {
            System.out.println(get(index));
        }
    }

    public static void main(String args[])
    {
        CircularList c = new CircularList();
        for (int i = 0; i < 10; i++)
            c.addToTail(new Integer(i));

        for (int i = 0; i < 10; i++)
            c.incrementCurrentPointer();

        c.dumpOnScreen();

        System.out.println(c.getCurrentObject());
        c.decrementCurrentPointer();
        System.out.println(c.getCurrentObject());
    }
}