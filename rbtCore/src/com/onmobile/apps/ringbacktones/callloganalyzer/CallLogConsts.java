/**
 * The field names and typical value constants in the call log.
 */

package com.onmobile.apps.ringbacktones.callloganalyzer;

public class CallLogConsts
{
    protected static final String KEY_CALLER_NUMBER = "CallerNumber";
    protected static final String KEY_CALLED_NUMBER = "CalledNumber";
    protected static final String KEY_ENT_SOKEY = "ENTERING_SOKEY";
    protected static final String KEY_SOKEY = "SOKEY";
    protected static final String KEY_EXIT_SOKEY = "EXITING_SOKEY";
    protected static final String KEY_STATUS = "STATUS";
    protected static final String KEY_NL_INT_00 = "NL_INTERPRETATION[0][0]";
    protected static final String KEY_NL_SLOT_00 = "NL_SLOT[0][0]";
    protected static final String KEY_CONFIDENCE_0 = "CONFIDENCE[0]";
    protected static final String UTTERANCE_FILENAME = "UTTERANCE_FILENAME";

    protected static final String VAL_STATUS_REC = "RECOGNITION";
    protected static final String VAL_STATUS_REJ = "REJECTED";
    protected static final String VAL_STATUS_UNRESOLVED = "<unresolved>";
    protected static final String VAL_STATUS_HANGUP = "HANG_UP";
    protected static final String VAL_STATUS_INTERPRETATION = "INTERPRETATION";
    protected static final String VAL_STATUS_NOSP = "NO_SPEECH_TIMEOUT";
    protected static final String VAL_STATUS_TMSP = "TOO_MUCH_SPEECH_TIMEOUT";
    protected static final String VAL_STATUS_ABORT = "ABORTED";
    protected static final String VAL_STATUS_NORESULT = "No Result";
    protected static final String FILTER_CALLERS = "Callers";
    protected static final String FILTER_APPS = "Apps";
    protected static final String FILTER_CALLED = "Called";
}