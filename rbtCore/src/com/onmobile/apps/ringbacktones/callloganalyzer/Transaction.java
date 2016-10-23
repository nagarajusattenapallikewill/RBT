/**
 * Transaction processing
 */

package com.onmobile.apps.ringbacktones.callloganalyzer;

public class Transaction
{
    protected String m_strTransName; // Name of the transaction.
    protected String m_strStartState; // Start State of the transaction.
    protected String m_strEndState; // End state of the transaction

    protected boolean m_bTransCompleted;// Indicate if both the start and end
    // states were encountered.
    protected boolean m_bTransStarted; // Indicate whether the transaction has
    // started.

    protected int m_iNumCompletedTransactions;
    protected int m_iNumFailedTransactions;

    public Transaction(String TransName, String StartState, String EndState)
    {
        m_strTransName = TransName;
        m_strStartState = StartState;
        m_strEndState = EndState;
        m_bTransCompleted = true;
        m_bTransStarted = false;
        m_iNumCompletedTransactions = 0;
        m_iNumFailedTransactions = 0;
    }

    // Reset the flags.
    public void resetFlags()
    {
        m_bTransCompleted = true;
        m_bTransStarted = false;
    }

    // Process one state.
    public void processState(String state)
    {

        if (state == null)
            return;

        if (m_bTransStarted)
        {
            if (state.equals(m_strEndState))
            {
                //System.out.println("Ended transaction with state" + state);
                m_bTransCompleted = true;
                m_bTransStarted = false;
                m_iNumCompletedTransactions++;
            }
        }
        else
        {
            if (state.equals(m_strStartState))
            {
                //System.out.println("Started transaction with state" + state);
                m_bTransStarted = true;
                m_bTransCompleted = false;
            }
        }
    }

    // Check whether the transaction has completed.
    public boolean hasCompleted()
    {
        return m_bTransCompleted;
    }

    // Check whether the transaction has started.
    public boolean hasStarted()
    {
        return m_bTransCompleted;
    }

    // Get the transaction Name
    public String getName()
    {
        return m_strTransName;
    }

    // Increment the failed trasaction count.
    public void incrementTransFailedCount()
    {
        m_iNumFailedTransactions++;
    }

    // Get number of completed transactions.
    public int getNumCompletedTransactions()
    {
        return m_iNumCompletedTransactions;
    }

    // Get number of failed transactions.
    public int getNumFailedTransactions()
    {
        return m_iNumFailedTransactions;
    }
}