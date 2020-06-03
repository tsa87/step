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
 * Delete a comment item in Datastore
 */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch("/delete-comment", {
      method: 'POST',
      body: params
    })
    .then(showComments)
}

/**
 * Render all comments in Datastore
 */
function showComments() {
  fetch("/list-comments")
    .then(response => response.json())
    .then((comments) => {
      const commentContainer = document.getElementById('comment-section');
      commentContainer.innerHTML = "";
      comments.forEach((comment) => {
        commentContainer.appendChild(createCommentItem(comment))
      });
    });
}

/**
 * Render a single comment
 */
function createCommentItem(comment) {

  const commentItem = document.createElement('div');

  const authorElement = document.createElement('h5');
  authorElement.innerText = "Author: " + comment.userName;

  const timeElement = document.createElement('h5');
  timeElement.innerHTML = "Time: " + comment.time;

  const likeElement = document.createElement('h5');
  likeElement.innerHTML = comment.like + " like(s)";

  const contentElement = document.createElement('p');
  contentElement.innerHTML = comment.content;

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment);
    commentItem.remove();
  });

  let htmlElements = [authorElement, timeElement, likeElement, contentElement, deleteButtonElement];
  htmlElements.forEach((htmlElement) => {
    commentItem.appendChild(htmlElement)
  });

  return commentItem;
}

/**
 * Auxiliary string comparsion function
 */
function compareString(a, b) {
  if (a.length !== b.length) {
    return false;
  }
  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) {
      return false;
    }
  }
  return true;
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

  if (!(compareString(stringTarget, currentString))) {
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

htmlInject('../head.html', "head")
  .then(() => {
    htmlInject('../header.html', ".meta-header")
  })
  .then(() => {
    return htmlInject('../footer.html', ".footer")
  })
  .then(() => {
    return setInterval(myTimer, 1000);
  })
  .then(() => {
    return setInterval(() => stumbleBuildString('MISSION', "#mission"), 50)
  })