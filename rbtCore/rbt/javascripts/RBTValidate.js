<SCRIPT LANGUAGE = "JavaScript">
//Checks fields empty , before Submit 
function fnSubmit(frmName)
{
	frmName.SUB_ID.disabled = false;
	if (!(isFieldAPhoneNumber(trim(frmName.SUB_ID.value))))
    {
		alert("Not a valid Subscriber ID "+trim(frmName.SUB_ID.value));
	    frmName.SUB_ID.focus();
	    return false;
	}
	if((frmName.callerID != null) && (frmName.callerID.disabled == false) && !(isFieldANumber(trim(frmName.callerID.value))))
	{
		alert("Not a valid Phone No. "+trim(frmName.callerID.value));
	    frmName.callerID.focus();
	    return false;
	}
	return true;
}

function fnCheck(frmName)
{
	if (isEmpty(frmName.corp_name.value))
    {
		alert("Corporate Tune must not be empty");
	    frmName.corp_name.focus();
	    return false;
	}
	if(isEmpty(frmName.browse.value)){
		alert("Specify the Wave File");
	    frmName.browse.focus();
	    return false;
	}else if(frmName.browse.value.indexOf(".wav") == -1){
		alert("Enter only wave file");
		frmName.browse.focus();
	    return false;
	}
	return true;
}

function fnChange(frmName){
	frmName.method="post";
	frmName.action="rbt_subs_selections.jsp";
	frmName.submit();
	return true;
}

function fnChange1(frmName){
	frmName.method="post";
	frmName.action="rbt_categories.jsp";
	frmName.submit();
	return true;
}

function fnChangeTel(frmName){
	frmName.method="post";
	frmName.updated.value="true";
	frmName.action="rbt_telecalling.jsp";
	frmName.submit();
	return true;
}

function isFieldANumber(strfield)
{
  //re = /\d/
  if (isEmpty(strfield))
     return false;
  re = /[0-9]/
  for(i=0; i< strfield.length; i++)
  {
    if (!isMatch(strfield.charAt(i),re))
    {
   	//     alert("Is Not a Number");
     return false;
    }
  }
  //   alert("Is a Number");
  return true;
}

function isEmpty(str)
{
	if( (str == null) || (str == "") || (str.length == 0) )
	{
		return true;
	}
	else
	{
		return false;
	}
}

/******************************************************************************
* function: ltrim(string)
*	  ltrims the string 	 
* 
*******************************************************************************/
function ltrim (string )
{
	return string.replace( /^\s*/, "" )
}
/******************************************************************************
* function: rtrim(string)
*	  ltrims the string 	 
* 
*******************************************************************************/
function rtrim ( string )
{
	return string.replace( /\s*$/, "" );
}

/******************************************************************************
* function: trim(string)
*	  trims the string 	 
* 
*******************************************************************************/
function trim ( string )
{
	return rtrim(ltrim(string));
}

/*********************************************************************************
	This function is used to check if the phone number field entered is valid
	Returns		: TRUE, if the phone field is valid (number or '-')
			: FALSE, otherwise.
	Creation	: wyu 12/07/2001
**********************************************************************************/
function isFieldAPhoneNumber(strfield)
{
  if (isEmpty(strfield))
     return false;
  re = /[0-9]/
  if(strfield.length < 7 || strfield.length > 15)
	  return false;
  for(i=0; i< strfield.length; i++)
  {
    if (!isMatch(strfield.charAt(i),re))
    {
     // Is Not a Phone Number
     return false;
    }
  }
  // Is a Phone Number
  return true;
}

function isFieldACallerID(strfield)
{
  if (isEmpty(strfield))
     return false;
  re = /[0-9]/
  if(strfield.length < 7 || strfield.length > 15)
	  return false;
  for(i=0; i< strfield.length; i++)
  {
    if (!isMatch(strfield.charAt(i),re))
    {
     // Is Not a Phone Number
     return false;
    }
  }
  // Is a Phone Number
  return true;
}

/******************************************************************************
	 This function compares given string and matches with the Regular Expression
	 Returns		: TRUE, if the value matches pattern
				: FALSE, otherwise.
******************************************************************************/

function isMatch(strValue,reg)
{
 if (reg.test(strValue)) {
//    alert("Pattern Matched");
    return true;
  }
//	alert("Pattern Differs");
	return false;
}

function fnSetFocus(frmName,index)
{
	 if (frmName != null)
	   frmName.elements[index].focus();
}

</script>
