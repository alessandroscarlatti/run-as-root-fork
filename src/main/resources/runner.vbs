'---------------------------------------
'Elevate this script before invoking it.
'25.2.2011 FNL
'---------------------------------------
bElevate = False
if WScript.Arguments.Count > 0 Then If WScript.Arguments(WScript.Arguments.Count-1) <> "|" then bElevate = True
if bElevate Or WScript.Arguments.Count = 0 Then ElevateUAC
'******************
'Your script goes here
'******************

'dim shell
'set shell=createobject("wscript.shell")
'shell.run "java --version", 1, True
'set shell=nothing
stdout = Console("COMMAND_TO_RUN")

'-----------------------------------------
'Run this script under elevated privileges
'-----------------------------------------
Sub ElevateUAC
    sParms = " |"
    If WScript.Arguments.Count > 0 Then
            For i = WScript.Arguments.Count-1 To 0 Step -1
            sParms = " " & WScript.Arguments(i) & sParms
        Next
    End If
    Set oShell = CreateObject("Shell.Application")
    returnValue = oShell.ShellExecute("wscript.exe", WScript.ScriptFullName & sParms, , "runas", 1)
    MsgBox returnValue
    WScript.Quit
End Sub

Function Console(strCmd)
'@description: Run command prompt command and get its output.
'@author: Jeremy England ( SimplyCoded )
  Dim Wss, Cmd, Return, Output
  Set Wss = CreateObject("WScript.Shell")
  Set Cmd = Wss.Exec("cmd.exe")
  Cmd.StdIn.WriteLine strCmd & " 2>&1"
  Cmd.StdIn.WriteLine "pause" & " 2>&1"
  Cmd.StdIn.Close
  While InStr(Cmd.StdOut.ReadLine, ">" & strCmd) = 0 : Wend
  Do : Output = Cmd.StdOut.ReadLine
    If Cmd.StdOut.AtEndOfStream Then Exit Do _
    Else Return = Return & Output & vbLf
  Loop
  Console = Return
End Function