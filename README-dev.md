<!--
  Licensed to ObjectStyle LLC under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ObjectStyle LLC licenses
  this file to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  -->
# Internal Notes for "bootique-swagger" Developers 

## On "swagger-ui" Integration

"bootique-swagger-ui" integrates some HTML/CSS/JS from the official [swagger-ui](https://github.com/swagger-api/swagger-ui)
project. The process is to go to GitHub releases, grab the release we need, and copy over the contents of the "dist" 
folder. "index.html" is merged into our "index.mustache", and the rest of the files go to the "docroot/static" folder.

We have no consistent way to track "swagger-ui" version. One way to go about it is to check 
`bootique-swagger/RELEASE-NOTES.md` where all upgrades are recorded.
