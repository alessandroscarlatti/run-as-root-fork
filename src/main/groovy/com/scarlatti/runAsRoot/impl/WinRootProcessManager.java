/**
 * ****************************************************************************
 * Copyright 2017 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package com.scarlatti.runAsRoot.impl;

import com.scarlatti.runAsRoot.NotAuthorizedException;
import com.scarlatti.runAsRoot.RootProcessManager;
import com.scarlatti.runAsRoot.UserCanceledException;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Elevate out process in Windows platform.
 *
 * @author dyorgio
 */
public class WinRootProcessManager implements RootProcessManager {

//    private static final String COMMAND_TO_ADMIN = "@echo off\n"
//            + "\n"
//            + ":: BatchGotAdmin\n"
//            + ":-------------------------------------\n"
//            + "REM  --> Check for permissions\n"
//            + ">nul 2>&1 \"%SYSTEMROOT%\\system32\\cacls.exe\" \"%SYSTEMROOT%\\system32\\config\\system\"\n"
//            + "\n"
//            + "REM --> If error flag set, we do not have admin.\n"
//            + "if '%errorlevel%' NEQ '0' (\n"
//            + "    echo Requesting administrative privileges...\n"
//            + "    goto UACPrompt\n"
//            + ") else ( goto gotAdmin )\n"
//            + "\n"
//            + ":UACPrompt\n"
//            + "    echo Set UAC = CreateObject^(\"Shell.Application\"^) > \"%temp%\\getadmin.vbs\"\n"
//            + "    set params = %*:\"=\"\"\n"
//            + "    echo UAC.ShellExecute \"cmd.exe\", \"/c %~s0 %params%\", \"\", \"runas\", 1 >> \"%temp%\\getadmin.vbs\"\n"
//            + "\n"
//            + "    \"%temp%\\getadmin.vbs\"\n"
//            + "    del \"%temp%\\getadmin.vbs\"\n"
//            + "    exit /B\n"
//            + "\n"
//            + ":gotAdmin\n"
//            + "    pushd \"%CD%\"\n"
//            + "    CD /D \"%~dp0\"\n"
//            + ":--------------------------------------\n";

    private static String COMMAND_TO_ADMIN;
    private static String VBSCRIPT_TO_ADMIN;

    static {
        try {
            COMMAND_TO_ADMIN = new String(Files.readAllBytes(Paths.get(WinRootProcessManager.class.getResource("/runner.bat").toURI())));
            VBSCRIPT_TO_ADMIN = new String(Files.readAllBytes(Paths.get(WinRootProcessManager.class.getResource("/runner.vbs").toURI())));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ProcessBuilder create(List<String> commands) throws Exception {

        File file = File.createTempFile("tmp", "run-as-root.bat");

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("cd %s\\bin%n", System.getProperty("java.home")));

        for (String command : commands) {
            builder.append(command).append(' ');
        }

        builder.append(COMMAND_TO_ADMIN);

        try (FileOutputStream out = new FileOutputStream(file)) {

            String script = builder.toString();
            System.out.println("script: " + script);

            out.write(script.getBytes());
            out.flush();
        }

        return new ProcessBuilder("cmd.exe", "/c ", file.getAbsolutePath()).inheritIO();
    }

    @Override
    public void handleCode(int code) throws NotAuthorizedException, UserCanceledException {
        System.err.println("CODE:" + code);
        if (code != 0) {
            throw new RuntimeException("Error executing process! CODE: " + code);
        }
    }
}
