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

import java.util.ArrayList;
import java.util.List;

/**
 * Elevate out process in Linux platform.
 *
 * @author dyorgio
 */
public class LinuxRootProcessManager implements RootProcessManager {

    @Override
    public ProcessBuilder create(List<String> commands) {
        List<String> copy = new ArrayList<>();
        copy.add("pkexec");
        copy.addAll(commands);
        return new ProcessBuilder(copy).inheritIO();
    }

    @Override
    public void handleCode(int code) throws NotAuthorizedException, UserCanceledException {
        switch (code) {
            case 127:
                throw new NotAuthorizedException();
            case 126:
                throw new UserCanceledException();
        }
    }
}
