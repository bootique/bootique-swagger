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

"bootique-swagger-ui" integrates HTML/CSS/JS from [swagger-ui](https://github.com/swagger-api/swagger-ui) packaged
as a ["web jar"](https://github.com/webjars/swagger-ui). When upgrading to a new version, check the `index.html` 
file inside the "web jar", and update our own "index.mustache" to match the structure of this file. Our customizations 
include:

* Dynamic path generation for Swagger resources
* `window.onload` function to [load our own app Swagger model](https://github.com/swagger-api/swagger-ui/blob/master/docs/usage/installation.md)
