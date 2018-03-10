/** *****************************************************************************
 * Copyright 2017 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************** */
package com.scarlatti.runAsRoot.impl;

import com.scarlatti.runAsRoot.NotAuthorizedException;
import com.scarlatti.runAsRoot.RootProcessManager;
import com.scarlatti.runAsRoot.UserCanceledException;

import java.util.List;

/**
 * Elevate out process in macOS platform.
 *
 * @author dyorgio
 */
public class MacRootProcessManager implements RootProcessManager {

    @Override
    public ProcessBuilder create(List<String> commands) {
        StringBuilder builder = new StringBuilder();
        for (String command : commands) {
            builder.append(command).append(' ');
        }

        return new ProcessBuilder("osascript", "-e",//
                "do shell script \"" + builder + " 2>&1\" with administrator privileges").inheritIO();
    }

    @Override
    public void handleCode(int code) throws NotAuthorizedException, UserCanceledException {
        switch (code) {
            case 1:
                throw new UserCanceledException();
        }
    }

}
