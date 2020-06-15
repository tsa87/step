// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Show user the local time 
 */
function myTimer() {
  var d = new Date();
  document.querySelector("#time").innerHTML = d.toLocaleTimeString();
}

/**
 * Communicate with servlet
 */
function servletOperation(endpoint, comment, refreshMethod) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch(endpoint, {
    method: 'POST',
    body: params
  })
    .then(refreshMethod)
}

/**
 * Like a comment item in Datastore
 */
function likeComment(comment) {
  servletOperation("/like-comment", comment, showComments);
}

function likeImage(image) {
  servletOperation("/like-image", image, showImages);
}

/**
 * Delete a comment item in Datastore
 */
function deleteComment(comment) {
  servletOperation("/delete-comment", comment, showComments);
}

/**
 * Render all comments in Datastore
 */
function showComments() {
  let count = document.getElementById('count').value;
  fetch(`/list-comments?count=${count}`)
    .then(response => response.json())
    .then((comments) => {
      const commentContainer = document.getElementById('comment-section');
      commentContainer.innerHTML = "";
      comments.forEach((comment) => {
        commentContainer.appendChild(createCommentItem(comment))
      });
    })
}

/**
 * Create a text HTML element
 */
function createHTML(elementType, content) {
    const htmlElement = document.createElement(elementType);
    htmlElement.innerHTML = content;
    return htmlElement;
}

/**
 * Create a button
 */
function createButton(text, onclick) {
    const buttonElement = document.createElement('button');
    buttonElement.innerText = text;
    buttonElement.addEventListener('click', onclick);
    return buttonElement;
}

function getEmoji(sentiment) {
  if (sentiment < 0.33) return "&#128533;"
  else if (sentiment < 0.66) return "&#128527;"
  else return "&#128516;"
}

/**
 * Reduce HTML injections 
 */
 function cleanupInput(string) {
  return string.replace(/</g, "&lt;").replace(/>/g, "&gt;");
 }

/**
 * Render a single comment
 */
function createCommentItem(comment) {
  const emoji = getEmoji(comment.sentimentScore);  

  const authorElement = createHTML('h5', cleanupInput(comment.userName) + " " + emoji);

  const likeElement = createHTML('h5', comment.likeCount + " like");
  const timeElement = createHTML('h5', comment.creationTime);

  let headerHTML = document.createElement('div');
  headerHTML.className = "comment-row";
  let headerElements = [authorElement, timeElement, likeElement]
  headerElements.forEach((htmlElement) => {
    headerHTML.appendChild(htmlElement)
  });
  
  content = cleanupInput(comment.content);
  const contentElement = createHTML('h4', content);

  const deleteButtonElement = createButton('Delete', () => {
    deleteComment(comment);
    commentItem.remove();
  });

  const likeButtonElement = createButton('Like', () => likeComment(comment));

  let footerHTML = document.createElement('div');
  footerHTML.className = "comment-row";
  let footerElements = [likeButtonElement, deleteButtonElement];
  footerElements.forEach((htmlElement) => {
    footerHTML.appendChild(htmlElement)
  });

  let commentHTML = document.createElement('div');
  commentHTML.className = "comment";
  
  let commentElements = [headerHTML, contentElement, footerHTML];
  commentElements.forEach((htmlElement) => {
    commentHTML.appendChild(htmlElement)
  });

  return commentHTML;
}

/**
 * 
 */
function showImages() {
  fetch("/list-images")
    .then(response => response.json())
    .then(images => {
      const imageContainer = document.getElementById('image-container');
      imageContainer.innerHTML = ""
      for (const image of images) {
        const imageElement = createImageItem(image)
        imageContainer.appendChild(imageElement)
      } 
    });
}

function createImageItem(image) {
  const divElement = document.createElement('div');

  const imageElement = document.createElement('img');
  imageElement.src = image.imageUrl;
  
  const captionElement = createHTML('h5', "[" + image.userName + "]: " + image.caption);
  const timeElement = createHTML('h5', "Post time: " + image.creationTime);

  const likeRow = createHTML('div', "");
  likeRow.className = "like-row";
  const likeElement = createHTML('h5', image.likeCount + " like");
  const likeButtonElement = createButton('Like', () => likeImage(image));
  likeButtonElement.className = "pill";

  likeRow.appendChild(likeElement);
  likeRow.appendChild(likeButtonElement);

  const childElements = [imageElement, captionElement, timeElement, likeRow];
  childElements.forEach((htmlElement) => {
    divElement.appendChild(htmlElement)
  });

  return divElement;
}

/**
 * Build a text string one by one
 */
const stumbleBuildString = (stringTarget, htmlTarget) => {
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  const vocabSize = characters.length;

  let currentString = document.querySelector(htmlTarget).innerText;
  const currentStringLength = currentString.length

  const randomChar = characters[Math.floor(Math.random() * vocabSize)];

  if (stringTarget != currentString) {
    if ((currentStringLength === 0) || (currentString.substr(currentStringLength - 1, 1) === stringTarget.substr(currentStringLength - 1, 1))) {
      currentString = currentString + randomChar;
    } else {
      currentString = currentString.substr(0, currentStringLength - 1) + randomChar;
    }
  }
  document.querySelector(htmlTarget).innerHTML = currentString;
}

/**
 * Inject HTML template code.
 */
function htmlInject(templatePath, htmlTarget) {
  return fetch(templatePath)
    .then(response => {
      return response.text();
    })
    .then(text => {
      document.querySelector(htmlTarget).innerHTML += text;
    })
}

function buildAddImgModal() {
  var modal = document.getElementById("modal");
  var addImgBtn = document.getElementById("add-image-button");
  addImgBtn.onclick = function () {
    modal.style.display = "block";
  }
	
  var span = document.getElementsByClassName("close")[0];
  span.onclick = function() {
    modal.style.display = "none";
  }

  window.onclick = function(event) {
    if (event.target == modal) {
      modal.style.display = "none";
    }
  }
}

/**
 * Use javascript to build part of page
 */
function buildPage() {
htmlInject('../header.html', ".meta-header")  
  .then(() => {
    return htmlInject('../footer.html', ".footer")
  })
  .then(() => {
    return setInterval(myTimer, 1000);
  })
  .then(() => {
    return setInterval(() => stumbleBuildString('OBJECTIVE', "#objective"), 50)
  });
}

function onLoad() {
  buildPage();
}