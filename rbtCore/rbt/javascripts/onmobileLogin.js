<SCRIPT LANGUAGE = "JavaScript">
//Checks fields empty , before Submit 
function fnSubmit(frmName)
{
       if (isEmpty(trim(frmName.txtUser.value)))
       {
	     alert("User Name cannot be empty");
	     frmName.txtUser.focus();
	     return false;
	}
        if (isEmpty(frmName.txtPassword.value))
        {
	     alert("Password cannot be empty");
	     frmName.txtPassword.focus();
	     return false;
	}
	return true;
}

</SCRIPT>
