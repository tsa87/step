<%--
Copyright 2019 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
  String uploadUrl = blobstoreService.createUploadUrl("/image-upload-handler"); %>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <!--Google Font API-->
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Karla&effect=anaglyph" />
    <!--Social Media Links-->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" />
    <title>Tony Shen - Images</title>
    <link rel="stylesheet" href="css/images.css" />
    <link rel="stylesheet" href="css/style.css" />
  </head>
  <body onload="onLoad(); showImages(); buildAddImgModal();">
    <div class="meta-header"></div>

	<div class="add-section">
      <button id="add-image-button" class="pill">Add Image</button>
    </div>

 	<!-- https://www.w3schools.com/howto/tryit.asp?filename=tryhow_css_modal -->
    <div id="modal" class="modal">

      <!-- Modal content -->
      <div id="modal-content" class="modal-content">
        <h3>Upload an Image</h3>
        <span class="close">&times;</span>
        <form method="POST" enctype="multipart/form-data" action="<%= uploadUrl %>">
          <label for="text-name">Username (required)</label>
          <input type="text" name="username" placeholder="username" maxlength="20" required>
          <label for="caption">Caption (required) </label>
          <textarea name="caption" required></textarea>
          <label for="image">Share your image (required)</label>
          <input type="file" name="image" required>
          <button class="pill">Sumbit</button>
        </form>
      </div>

    </div>

    <div id="image-container">
    </div>

    <div class="footer"></div>
  </body>
</html>
<script src="js/script.js"></script>

