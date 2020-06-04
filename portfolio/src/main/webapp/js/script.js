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
function servletOperation(endpoint, comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch(endpoint, {
    method: 'POST',
    body: params
  })
    .then(showComments)
}

/**
 * Like a comment item in Datastore
 */
function likeComment(comment) {
  servletOperation("/like-comment", comment);
}

/**
 * Delete a comment item in Datastore
 */
function deleteComment(comment) {
  servletOperation("/delete-comment", comment);
}

/**
 * Render all comments in Datastore
 */
function showComments() {
  let count = document.getElementById('count').value;
  fetch("/list-comments?count="  + count)
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

/**
 * Render a single comment
 */
function createCommentItem(comment) {
  const authorElement = createHTML('h5', comment.userName);
  const likeElement = createHTML('h5', comment.likeCount + " like");
  const timeElement = createHTML('h5', comment.creationTime);

  let headerHTML = document.createElement('div');
  headerHTML.className = "comment-row";
  let headerElements = [authorElement, timeElement, likeElement]
  headerElements.forEach((htmlElement) => {
    headerHTML.appendChild(htmlElement)
  });
  
  const contentElement = createHTML('h4', comment.content);

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
 * Build a text string one by one
 */
const stumbleBuildString = (stringTarget, htmlTarget) => {
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  const vocabSize = characters.length;

  let currentString = document.querySelector('#mission').innerText;
  const currentStringLength = currentString.length

  const randomChar = characters[Math.floor(Math.random() * vocabSize)];

  if (stringTarget != currentString) {
    if ((currentStringLength === 0) || (currentString.substr(currentStringLength - 1, 1) === stringTarget.substr(currentStringLength - 1, 1))) {
      currentString = currentString + randomChar;
    } else {
      currentString = currentString.substr(0, currentStringLength - 1) + randomChar;
    }
  }
  document.querySelector('#mission').innerHTML = currentString;
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


htmlInject('../header.html', ".meta-header")  
  .then(() => {
    return htmlInject('../footer.html', ".footer")
  })
  .then(() => {
    return setInterval(myTimer, 1000);
  })
  .then(() => {
    return setInterval(() => stumbleBuildString('MISSION', "#mission"), 50)
  })