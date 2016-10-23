/**
 * The info associated with an application.
 */

package com.onmobile.apps.ringbacktones.callloganalyzer;

import java.util.ArrayList;
import java.util.HashMap;

public class AppInfo
{
    protected String m_strAppName; // The application name.
    protected ArrayList m_alCallStates; // List of CallStateInfo.
    protected ArrayList m_alTransList; // Lis tof transactions.
    protected int m_iNumStates; // Number of states.

    protected int m_iCompletedTransCount; // Number of completed transactions.
    protected int m_iFailedTransCount; // Number of failed transactions.

    protected class CallStateInfo
    {
        protected String m_strStateName; // State Name.
        protected int iNumCallRejections; // Number of rejection states.
        protected HashMap m_hmStatus; // Map for various status.

        public CallStateInfo(String stateName)
        {
            m_strStateName = stateName;
            iNumCallRejections = 0;
            m_hmStatus = new HashMap();

            m_iCompletedTransCount = 0;
            m_iFailedTransCount = 0;
        }

        // Add Info.
        public void addInfo(String status)
        {
            Integer IVal = (Integer) m_hmStatus.get(status);
            if (IVal == null)
            {
                // Create an object and put in the map.
                IVal = new Integer(1);
            }
            else
            {
                // Increment the value.
                IVal = new Integer(IVal.intValue() + 1);
            }

            m_hmStatus.put(status, IVal);
        }

        // Gte the value for a status.
        public int getInfo(String status)
        {
            Integer IVal = (Integer) m_hmStatus.get(status);
            if (IVal == null)
                return 0;
            return IVal.intValue();
        }

        // Increment the callrejections count.
        public void incrementCallRejections(int iVal)
        {
            iNumCallRejections += iVal;
        }

        // Get the number of call rejections.
        public int getCallRejections()
        {
            return iNumCallRejections;
        }

        // Get the state Name;
        public String getStateName()
        {
            return m_strStateName;
        }
    }

    public AppInfo(String appName)
    {
        m_strAppName = appName;
        m_iNumStates = 0;
        m_alCallStates = new ArrayList();
        m_alTransList = new ArrayList();
    }

    // Clear all data and reset flags.
    public void resetAll()
    {
        for (int i = 0; i < m_alTransList.size(); i++)
        {
            ((Transaction) m_alTransList.get(i)).resetFlags();
        }
    }

    // Initialise the transactions list.
    public void addTransaction(String TransName, String StartState,
            String EndState)
    {
        m_alTransList.add(new Transaction(TransName, StartState, EndState));
    }

    // Process the state to check for transactions.
    public void processTransaction(String soKey)
    {
        for (int i = 0; i < m_alTransList.size(); i++)
            ((Transaction) m_alTransList.get(i)).processState(soKey);
    }

    // Returns the name of the first failed transaction or null;
    public String anyFailedTrans()
    {
        // If no transactions are specified... return true.
        if (m_alTransList.isEmpty())
            return null;
        Transaction _tr;
        for (int i = 0; i < m_alTransList.size(); i++)
        {
            _tr = (Transaction) m_alTransList.get(i);
            if (_tr.hasCompleted())
            {
                continue;
            }
            // Else... failed transaction.
            _tr.incrementTransFailedCount();
            return _tr.getName();
        }
        return null;
    }

    // Get the application name.
    public String getAppName()
    {
        return m_strAppName;
    }

    // Get the number of states for the app.
    public int getNumStates()
    {
        return m_iNumStates;
    }

    // Return the list of state names.
    public String[] getStateNames()
    {
        String[] _arrStates = new String[m_iNumStates];
        for (int i = 0; i < m_iNumStates; i++)
        {
            _arrStates[i] = ((CallStateInfo) m_alCallStates.get(i))
                    .getStateName();
        }
        return _arrStates;
    }

    // Add information to the states.
    public void addInfo(String soKey, String status)
    {
        CallStateInfo csi = null;
        int i;
        for (i = 0; i < m_iNumStates; i++)
        {
            csi = (CallStateInfo) m_alCallStates.get(i);
            if (csi.getStateName().equals(soKey))
                break;
        }

        if (i == m_iNumStates)
        {
            // Not found.
            csi = new CallStateInfo(soKey);
            m_alCallStates.add(csi);
            m_iNumStates++;
        }

        csi.addInfo(status);
    }

    // Increment the callrejection count for the state.
    public void incrementCallRejections(String soKey, int iVal)
    {
        CallStateInfo csi = null;
        for (int i = 0; i < m_iNumStates; i++)
        {
            csi = (CallStateInfo) m_alCallStates.get(i);
            if (csi.getStateName().equals(soKey))
            {
                csi.incrementCallRejections(iVal);
                break;
            }
        }
    }

    // Get Call rejection count.
    public int getCallRejCount(String soKey)
    {
        CallStateInfo csi = null;
        for (int i = 0; i < m_iNumStates; i++)
        {
            csi = (CallStateInfo) m_alCallStates.get(i);
            if (csi.getStateName().equals(soKey))
                return csi.getCallRejections();
        }
        return 0;
    }

    // Get the information for a particular callstate.
    public int getInfo(String state, String status)
    {
        CallStateInfo csi = null;
        for (int i = 0; i < m_iNumStates; i++)
        {
            csi = (CallStateInfo) m_alCallStates.get(i);
            if (csi.getStateName().equals(state))
                return csi.getInfo(status);
        }
        return 0;
    }

    // Get number of successful transactions.
    public int getNumCompletedTransactions(String transName)
    {
        Transaction tr;
        for (int i = 0; i < m_alTransList.size(); i++)
        {
            tr = (Transaction) m_alTransList.get(i);
            if (transName.equals(tr.getName()))
                return tr.getNumCompletedTransactions();
        }
        return 0;
    }

    // Get the number of failed transactions.
    public int getNumFailedTransactions(String transName)
    {
        Transaction tr;
        for (int i = 0; i < m_alTransList.size(); i++)
        {
            tr = (Transaction) m_alTransList.get(i);
            if (transName.equals(tr.getName()))
                return tr.getNumFailedTransactions();
        }
        return 0;
    }
}