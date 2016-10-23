<SCRIPT LANGUAGE = "JavaScript">
/**
 *  OnMobileValidate.js
 *
 *  javascript File for the input validations in the OnMobile Administrator Console
 *
 *  Copyright (c) 2000, OnMobile Inc.  All Rights Reserved.
 *
 *  @version 0.1 OnMobileValidate.js, 28/06/2001 pradeep_c@infy.com
 *
 */

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

/******************************************************************************
	 This function checks for Null values of Text and Passord fields in the Form
	 Returns		: TRUE, if the form has Empty fields
				: FALSE, otherwise.
******************************************************************************/
function isFormNull(frmForm)
{
       for(var i=0;i<frmForm.elements.length;i++)
		{
		if(((frmForm.elements[i].type=="text" || frmForm.elements[i].type=="password"))
		   && !isEmpty(frmForm.elements[i].value))
			{
			  return false;
			}
		}
	return true;
}
    
/******************************************************************************
	 This function checks for a Space in  a given field
	 Returns		: TRUE, if the field has.
					: FALSE, otherwise.
******************************************************************************/
function hasSpace(strField)
{
  re = /\W+/
  if (isMatch(strField,re))
  {
     //alert("Has Space");
     return true;
  }
  else
  {
     //  alert("Has no Space");
       return false;
   }
 }



/******************************************************************************
	 This function checks for Null value of a given field
	 Returns		: TRUE, if the field is null.
				: FALSE, otherwise.
******************************************************************************/
function isFieldNull(strfield)
{
  re = /^$/
  if (isMatch(strfield,re))
  {
//     alert(" Is Null");
     return true;
  }
  else
  {
//       alert("Is Not Null");
       return false;
   }
}
/******************************************************************************
	This function checks for Number value of a given field
	Returns		: TRUE, if the field caries a number.
			: FALSE, otherwise.
******************************************************************************/

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


/******************************************************************************
	This function checks whether the given field is a Number (can have a '+' in
	front of it)
	Returns		: TRUE, if the field caries a number.
			: FALSE, otherwise.
******************************************************************************/

function isFieldAnExtNumber(strfield)
{
  //re = /\d/
  if (isEmpty(strfield))
     return false;
  reExt = /[0-9+]/
  re = /[0-9]/
  if ( !isMatch (strfield.charAt(0), reExt) )
  {
	  return false;
  }
  for(i=1; i< strfield.length; i++)
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




/******************************************************************************
	This function is used to check if the field is empty.
	Returns		: TRUE, if the field is empty.
			: FALSE, otherwise.
******************************************************************************/
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
	This function is used to check if the email field entered is valid
	Returns		: TRUE, if the email field is valid.
			: FALSE, otherwise.
******************************************************************************/
function isValidEmail(checkString)
{
    var newstr = "";
    var at = false;
    var dot = false;

    // DO SOME PRELIMINARY CHECKS ON THE DATA

    // IF EMAIL ADDRESS HAS ONLY ONE '@' CHARACTER
    var iindex = checkString.indexOf("@")
    if (iindex != -1)
    {
      if (checkString.indexOf("@",iindex+1) == -1)
      {
		at = true;
		// IF EMAIL ADDRESS HAS A '.' CHARACTER
		if (checkString.indexOf(".") != -1)
			dot = true;
		else
		 	return false;
	  }
	  else
	    return false;
	}
    else
       return false;
    
    // PARSE REMAINDER OF STRING
    for (var i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i + 1)
        if ((ch >= "A" && ch <= "Z") || (ch >= "a" && ch <= "z")
                || (ch == ".") || (ch == "_") || (ch == "@")
                || (ch == "-") || (ch >= "0" && ch <= "9")) {
                newstr += ch;
                if (ch == ".") {
                    dot=true;
                }
        }
    }
    if ((at == true) && (dot == true) && newstr == checkString) {
        return true;
        //return newstr;
    }
    else {
      // DISPLAY ERROR MESSAGE
        //alert ("Sorry, the email address you\nentered is not in the correct\nformat.");
		return false;
      //return checkString;
    }
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
  re = /[0-9,-]/
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
* This functiont tells whether the given date is valid or not
*           This function expects date in the format of 
*           dd-mmm-yyyy  
******************************************************************************/
function isProperDate(argDate) {
	var tmpDay = getDay(argDate);
	var tmpMon = getMonth(argDate);
	if (tmpMon.length != 3)
	  return false;
	  
	
	var MonthNo = isMonthStringValid(tmpMon);
	var tmpYear = getYear(argDate);
	
	if (tmpYear.length != 4)
	   return false;
	return isProperDay(tmpDay, MonthNo, tmpYear) && isProperMonth(MonthNo) && isProperYear(tmpYear)
}

/******************************************************************************
* function: isWhiteSpace
*           Function to check whether the given argument consists of charactes other
*           than a space and \t
******************************************************************************/
function isWhiteSpace(argWhiteSpace) {
	argWs = argWhiteSpace.toString()
	
	for (var intI=0; intI < argWs.length; intI++)
		if (argWs.charAt(intI) != ' ' && argWs.charAt(intI) != '\t')
			return false
	
	return true
}

/******************************************************************************
* function: isLeapYear
*           Function to tell, whether the given year is leap year or not
******************************************************************************/
function isLeapYear(argYear) {
	return ((argYear % 4 == 0) && (argYear % 100 != 0)) || (argYear % 400 == 0)
}

/******************************************************************************
 function: daysInMonth
           Function to return the maximum number of days in a given month of a
           given year
******************************************************************************/
function daysInMonth(argMonth, argYear) {
	switch (Number(argMonth)) {
		case 1:		// Jan
		case 3:		// Mar
		case 5:		// May
		case 7:		// Jul
		case 8:		// Aug
		case 10:		// Oct
		case 12:		// Dec
			return 31;
			break;
		
		case 4:		// Apr
		case 6:		// Jun
		case 9:		// Sep
		case 11:		// Nov
			return 30;
			break;
		
		case 2:		// Feb
			if (isLeapYear(argYear))
				return 29
			else
				return 28
			break;
		
		default:
			return 0;
	}
}

/******************************************************************************
* function: getDateSeparator
*           Function to return the date separator
*          This function expects date in the format 
*          dd-mmm-yyyy 
******************************************************************************/
function getDateSeparator(argDate) {
	// Are there invalid separators?
	if (argDate.indexOf('-') > 0)
		return '-'
	else
		return ' '
}

/******************************************************************************
* function: getYear
*           Function to return the year part of the given date.
*           This function expects date in the format of 
*           dd-mmm-yyyy
******************************************************************************/
function getYear(argDate) {
	var dateSep = getDateSeparator(argDate)
	
	if (dateSep == ' ')
		return 0

	if(argDate.split(dateSep).length == 3)
		return argDate.split(dateSep)[2]
	else
		return 0
}

/******************************************************************************
* function: getMonth
*           Function to return the month part of the given date.
*           This function expects date in the format of 
*           dd-mmm-yyyy
******************************************************************************/
function getMonth(argDate) {
	var dateSep = getDateSeparator(argDate)
	
	if (dateSep == ' ')
		return 0

	if(argDate.split(dateSep).length == 3)
		return argDate.split(dateSep)[1]
	else
		return 0
}

/******************************************************************************
* function: getDay
*           Function to return the day part of the given date.
*           This function expects date in the format of 
*           dd-mmm-yyyy
******************************************************************************/
function getDay(argDate) {
	var dateSep = getDateSeparator(argDate)
	
	if (dateSep == ' ')
		return 0

	if(argDate.split(dateSep).length == 3)
		return argDate.split(dateSep)[0]
	else
		return 0
}

/******************************************************************************
*  function: isProperDay
*           Function to tell whether the given day of the given month is valid
******************************************************************************/
function isProperDay(argDay, argMonth, argYear) {
	if ((isWhiteSpace(argDay)) || (argDay == 0))
		return false

	if ((argDay > 0) && (argDay < daysInMonth(argMonth, argYear) + 1))
		return true
	else
		return false
}

/******************************************************************************
* function: isProperMonth
*           Function to tell whether the given month is a valid one
******************************************************************************/
function isProperMonth(argMonth) {
	if ((isWhiteSpace(argMonth)) || (argMonth == 0))
		return false
	
	if ((argMonth > 0) && (argMonth < 13))
		return true
	else
		return false
}

/******************************************************************************
* function: isProperYear
*           Function to tell whether the given Year is a valid one
*******************************************************************************/
function isProperYear(argYear) {
	if ((isWhiteSpace(argYear)) || (argYear.toString().length > 4) || (argYear.toString().length == 3))
		return false
	
	switch (argYear.toString().length) {
		case 1:
			if (argYear >=0 && argYear < 10)
				return true
			else
				return false
			
		case 2:
			if (argYear >=0 && argYear < 100)
				return true
			else
				return false
			
		case 4:
			if (((argYear >= 1800) || (argYear >=2000)) && ((argYear <= 3000) || (argYear < 2000)))
				return true
			else
				return false
		
		default:
			return false
	}
}
/******************************************************************************
* function: getTime
*           Function to get Time from date & time field
*            Seperated by Space
*******************************************************************************/
function getTime(strDateTime)
{
	dateSep = ' ';

	if(strDateTime.split(dateSep).length == 2)
		return strDateTime.split(dateSep)[1]
	else
		return -1
}
/******************************************************************************
* function: getTime
*           Function to get Date from date & time field
*	    Seperated by Space
*******************************************************************************/

function getDate(strDateTime)
{
	dateSep = ' ';

	if(strDateTime.split(dateSep).length == 2)
		return strDateTime.split(dateSep)[0]
	else
		return -1
}
/******************************************************************************
* function: isTimeValidSecs
*           Function to check whether Time given is Valid
            Checks for hour minute and Seconds
*******************************************************************************/
function isTimeValidSecs(strTime)
{
    var hh = getHour(strTime);
    var mm = getMinute(strTime);
    var ss = getSecond(strTime);
    if (hh  != -1)
		if (mm != -1)
			if (ss != -1)
			{
			   if (hh >= 0 && hh< 24)
			     if (mm >= 0 && mm < 60)
			        if (ss >= 0 && ss < 60)
			          return true;
			 }
			 return false;

}
/******************************************************************************
* function: isTimeValid
*           Function to check whether Tiem given is Valid
            Checks for hour and minute only.
*******************************************************************************/
function isTimeValid(strTime)
{
    var hh = getHour(strTime);
    var mm = getMinute(strTime);
    //var ss = getSecond(strTime);
    
    if (hh  != -1)
		if (mm != -1)
			//if (ss != -1)
			{
			   if (hh >= 0 && hh< 24)
			     if (mm >= 0 && mm < 60)
			        //if (ss >= 0 && ss < 60)
			          return true;
			 }
			 return false;

}
/******************************************************************************
* function: getHour
*           Function to get Hour time field
*******************************************************************************/

function getHour(strTime) {
	if(strTime.split(":").length == 2 || strTime.split(":").length == 3)
		return strTime.split(":")[0]
	else
		return -1
}
/******************************************************************************
* function: getMinute
*           Function to get Minute from  time field
*******************************************************************************/

function getMinute(strTime) {
	if(strTime.split(":").length == 2 || strTime.split(":").length == 3)
		return strTime.split(":")[1]
	else
		return -1
}
/******************************************************************************
* function: getSecond
*           Function to get Second from time field
*******************************************************************************/

function getSecond(strTime) {
	if(strTime.split(":").length == 3)
		return strTime.split(":")[2]
	else
		return -1
}
/******************************************************************************
* function: isDateandTimeValid
*           Function to check whether Date and Time valid
*           Seperated by Space
*******************************************************************************/

function isDateandTimeValid(strDateTime)
{
  if (checkdateFormat(strDateTime))
  {
	  date = getDate(strDateTime);
	  time = getTime(strDateTime);
	  if ((date != -1) && (time != -1))
	    if (isProperDate(date) && isTimeValidSecs(time))
   	        return true;
   }
   return false;
}
/*******************************************************************************
* function: getErrorMessage
*           Function to display Error Messages
*******************************************************************************/
function getErrorMessage(strErrCode,strField)
{
  OM_NULL_EMPTY  =" cannot be null or empty";
  OM_HAS_SPACE   =" should not contain space characters" ;
  OM_INV_DATE    =" entered is Invalid"
  OM_INV_NUMBER  =" Not a valid Number";
  OM_DATE_GREATER  = " Greater than ";
  
  
  switch(strErrCode)
  {
     case "OM_NULL_EMPTY" : return strField+OM_NULL_EMPTY;
     case "OM_HAS_SPACE" : return strField+OM_HAS_SPACE;
     case "OM_INV_DATE" : return strField+OM_INV_DATE;
     case "OM_INV_NUMBER" : return strField+OM_INV_NUMBER;
     case "OM_DATE_GREATER"  : return strField+OM_DATE_GREATER;
     default: return -1;
   }
}
/*******************************************************************************
* function: fnSetFocus
*           Function to set Focus to a particular element in the Form
*******************************************************************************/
function fnSetFocus(frmName,index)
{
	 if (frmName != null)
	   frmName.elements[index].focus();
}

/*******************************************************************************
* function: fnClearAll
*           Function to clear all the fields in A form except the parameter given
           and sets focus for the secomnd parameter
*******************************************************************************/
function fnClearButOne(frmName,index,indFocus)
{
    for(i=0;i< frmName.elements.length;i++)
    {
        var strType = frmName.elements[i].type;

    	if ((strType != "select-one") && (i != index))
       {
          frmName.elements[i].value ="";
       }
    }
    frmName.elements[indFocus].focus();
    
    return false;
}


/*******************************************************************************
* function: fnClearAll
*           Function to clear all the fields in the form
*******************************************************************************/
function fnClearAll(frmName)
{
   for(i=0;i< frmName.elements.length;i++)
   {
    var strType = frmName.elements[i].type;
    if (strType != "select-one" && strType != "hidden")
         frmName.elements[i].value="";
   }
   frmName.elements[0].focus();
   return false;
}

/*******************************************************************************
* function: fnDateCompare
*           Function to compare two valid dates of type dd-mmm-yyyy hh:mm:ss
*******************************************************************************/
function fnDateTimeCompare(dtstrStart,dtstrEnd)
{
         
	var tmpdate  = getDate(dtstrStart);
  	var tmptime  = getTime(dtstrStart);
        var tmpDay   = getDay(tmpdate);
        var tmpMonth = isMonthStringValid(getMonth(tmpdate));
        var tmpYear  = getYear(tmpdate);
        var tmpHour  = getHour(tmptime);
        var tmpMin   = getMinute(tmptime);
        var tmpSec   = getSecond(tmptime);
        
        dtStart = new Date(tmpYear,tmpMonth,tmpDay,tmpHour,tmpMin,tmpSec);
        
        tmpdate  = getDate(dtstrEnd);
        tmptime  = getTime(dtstrEnd);
        tmpDay   = getDay(tmpdate);
        tmpMonth = isMonthStringValid(getMonth(tmpdate));
        tmpYear  = getYear(tmpdate);
        tmpHour  = getHour(tmptime);
        tmpMin   = getMinute(tmptime);
        tmpSec   = getSecond(tmptime);
        
        dtEnd = new Date(tmpYear,tmpMonth,tmpDay,tmpHour,tmpMin,tmpSec);
        
        if (dtStart < dtEnd)
	    return true;
	else
	   return false;
}
/*******************************************************************************
* function: fnDateCompare
*           Function to compare two valid dates of type dd-mmm-yyyy
*******************************************************************************/
function fnDateCompare(dtstrStart,dtstrEnd)
{
        
	 var tmpDay   = getDay(dtstrStart);
	 var tmpMonth = isMonthStringValid(getMonth(dtstrStart));
	 var tmpYear  = getYear(dtstrStart);

	 dtStart = new Date(tmpYear,tmpMonth,tmpDay,00,00,00);

	 
	 tmpDay   = getDay(dtstrEnd);
	 tmpMonth = isMonthStringValid(getMonth(dtstrEnd));
	 tmpYear  = getYear(dtstrEnd);
	 
	 dtEnd = new Date(tmpYear,tmpMonth,tmpDay,00,00,00);

	 if (dtStart<dtEnd)
	    return true;
	else
   	   return false;

}

/******************************************************************************
* function: isMonthStringValid
*           Function to check whether Month String is Valid
*	    Returns the corresponding month no if true -1 otherwise
*******************************************************************************/
function isMonthStringValid(strMonth)
{
  var strMon = strMonth;
  strMon = strMon.toUpperCase();

  switch(strMon)
  {

	case "JAN" : return "1";
	case "FEB" : return "2";
	case "MAR" : return "3";
	case "APR" : return "4";
	case "MAY" : return "5";
	case "JUN" : return "6";
	case "JUL" : return "7";
	case "AUG" : return "8";
	case "SEP" : return "9";
	case "OCT" : return "10";
	case "NOV" : return "11";
	case "DEC" : return "12";
	default: return -1;
  }
}
/******************************************************************************
* function: getCurrentDate()
*           Function to get the current Date of format dd-mmm-yyyy
*******************************************************************************/
function fnGetCurrentDate()
{

  var months = "JAN FEB MAR APR MAY JUN JUL AUG SEP OCT NOV DEC";
  var cur_date = new Date();
  index = cur_date.getMonth()*4;
  day = cur_date.getDate();
  if (day.length < 2)
     day = "0" + day;
  var strDate = day+"-" + months.substring(index,index+3) + "-" +
  				cur_date.getYear();
  return strDate;
}
/******************************************************************************
* function: fnisValidIPAddress()
*           Function checks for a valid IP
*******************************************************************************/
function fnisValidIPAddress(strIP)
{
  
  if(strIP.split(".").length != 4)
     return false;
  for (j=0;j<4;j++)
     {
        strAdd = strIP.split(".")[j];
        if (!isFieldANumber(strAdd))
        	return false;
     }
  return true;
}
/******************************************************************************
* function: fnisDateWithinRange()
*	  Checks whether date fall between 01-jan-1970 && 01-jan-2037
*	  for a valid date
*******************************************************************************/
function fnisDateWithinRange(dDate)
{
  
  startDate = "01-Jan-1970 00:00:00"
  endDate   = "01-Jan-2037 00:00:00"
  if (fnDateTimeCompare(dDate,startDate))
  	return false;
  if (fnDateTimeCompare(endDate,dDate))
  	return false;  	
  return true;	
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
/******************************************************************************
* function: checkdateFormat()
*           returns whether the date format of the string given is correct or not
*           date format used to check is dd-mmm-yyyy hh:mm:ss  -the time field is 
*	    optional
*******************************************************************************/
function checkdateFormat(strdate)
{
   if (strdate.length <= 11)
		re = /\d{2}-[a-zA-Z]{3}-\d{4}$/
   else
		re = /\d{2}-[a-zA-Z]{3}-\d{4} \d{2}:\d{2}:\d{2}$/
   if (re.test(strdate))
      return true;
   else
      return false;
}


function fnContainsDBIllegalChars(strVal)
{
	reg=/[\'`|]+/;
	if(reg.test(strVal))
		return true;
	else
		return false;
}

function fnValidateServiceName(strServName)
{
	reg=/[^\w@\._-]+/;
	if(isMatch(strServName,reg))
  	    return false;		
	else
           return true;
}

</SCRIPT>
