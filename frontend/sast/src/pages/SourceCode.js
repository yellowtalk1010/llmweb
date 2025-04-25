'use client';
import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"
import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { Marked } from "marked"; 

import './resources/float_win.js'

import '../float_window.css'
import '../cpp.css'
// import '../marked.min.js'
// import '../float_window.js'

// function renderFinished() {
//   console.log("DOM加载完成");

//     const buttons = document.querySelectorAll('.btn');
//     var websocketResult = null;
//     // 创建 WebSocket 连接（仅一个连接复用）
//     const socket = new WebSocket("ws://localhost:8080/ws");

//     socket.addEventListener('open', () => {
//         console.log('✅ WebSocket 连接已打开');
//     });

//     socket.addEventListener('error', (err) => {
//         console.error('❌ WebSocket 连接出错', err);
//     });
    
//     socket.onmessage = function (event) {
//         console.log("收到消息: " + event.data);
//         console.log(websocketResult)

//         //将数据写入到隐藏输入框中
//         document.getElementById("textarea_hidden-" + websocketResult).value
//             = document.getElementById("textarea_hidden-" + websocketResult).value + event.data

//         const input = document.getElementById("textarea_hidden-" + websocketResult).value;
//         const html = Marked.parse(input);
//         console.info("html:" + html)
//         document.getElementById("result-" + websocketResult).innerHTML = html;
//     };

//     buttons.forEach((btn, index) => {
//         var issueId = btn.id
//         // console.info(issueId)
//         // 创建对应的悬浮层
//         const popup = document.createElement('div');
//         popup.className = 'popup';
//         popup.innerHTML = `
//           <div>AI审计</div>
//           <textarea placeholder="请输入..." rows="4" class="textarea"></textarea>
//           <br>
//           <button class="check-btn">审计</button> 
//           <button class="close-btn">关闭</button>
//           <br>
//           <textarea class="textarea_hidden" id="textarea_hidden-${issueId}" hidden="hidden"></textarea>
//           <br>
//           <div class="result" id="result-${issueId}"></div>
//         `;
//         document.body.appendChild(popup);

//         const textarea = popup.querySelector('.textarea');
//         const checkBtn = popup.querySelector('.check-btn');
//         const closeBtn = popup.querySelector('.close-btn');
//         const result = popup.querySelector('.result') //全局变量


//         // 点击按钮显示悬浮层
//         btn.addEventListener('click', () => {
//             const rect = btn.getBoundingClientRect();
//             popup.style.left = rect.right + 10 + 'px';
//             popup.style.top = rect.top + window.scrollY + 'px';
//             popup.style.display = 'block';

//             // 可选：自动聚焦输入框
//             popup.querySelector('input').focus();
//         });

//         // 点击“关闭”隐藏悬浮层
//         closeBtn.addEventListener('click', () => {
//             websocketResult = null
//             popup.style.display = 'none';
//         });

//         checkBtn.addEventListener('click', () => {

//             websocketResult = issueId
//             var code = textarea.value
//             const str = issueId + "##########" + code
//             console.info(str)
//             socket.send(str);

//         })


//     });
// }


function SourceCode() {

  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const vtid = queryParams.get('vtid');
  const file = queryParams.get('file');

  console.info(vtid)
  console.info(file)

  const [sourceCodeData, setSourceCodeData] = useState({
    lines:[],
    issues:[],
    status: 0
  })
  if(sourceCodeData.status==0){
    fetch('/sourceCode_list?vtid='+vtid + '&file='+file, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res =>{
      const json = res.json();
      // console.info(json)
      return json
    }).then(data =>{
      console.log("rule_vtid 的数据")
      console.log(data)
      setSourceCodeData({
        lines: data.lines,
        issues: data.issues,
        status: 200
      })
      console.info("渲染完成后执行")
    }).catch(e =>{
      console.log(e)
    })
    
  }

  
  function clickAiCheck() {
    // event.preventDefault(); // 阻止跳转（如果需要）
    console.log("点击了链接！");
    alert("aaa")
  }

  const handleClick = () => {
    console.log("按钮点击成功！");
    alert("点击了按钮！");
  };

  function renderIssue1(lineIssues) {
    return lineIssues.map((issue, index) => (
      <div
        key={issue.id}
        className="floatDiv"
        style={{ backgroundColor: "pink", marginBottom: "8px" }}
      >
        <div>{issue.name}</div>
        <div>
          {issue.line}/{issue.vtId}/{issue.rule}/{issue.defectLevel}/{issue.defectType}
        </div>
        <div>{issue.ruleDesc}</div>
        <div>{issue.issueDesc}</div>
        <button onClick={clickAiCheck} id={issue.id}>AI审计</button>
      </div>
    ));
  }
  
  return (
    <>
        <div id="SourceCode">
          <ol>
            {
              sourceCodeData.lines.map((lineHtml, index) => {
                const lineNumber = index + 1;
                const lineIssues = sourceCodeData.issues.filter(issue=>issue.line==lineNumber) || []
                if(lineIssues!=null && lineIssues.length > 0){
                
                  const divDoms = renderIssue1(lineIssues) //渲染issue列表,将issue对象编程react元素列表（不是dom元素列表）
                  // console.info("divDoms:")
                  // console.info(divDoms)
                   
                  const span = document.createElement('span'); //
                  span.innerHTML = lineHtml;
                  // console.info("span:")
                  //console.info(span.firstChild) //将html字符串转成dom元素
 
                  const newLiElement = (
                    <span
                    key={index}
                    dangerouslySetInnerHTML={{ __html: lineHtml }} //将字符串转成 react元素
                    />
                  );
                  divDoms.unshift(newLiElement); //将新元素加入到列表中

                  return divDoms
                }
                else {
                  return (
                    <span
                    key={index}
                    dangerouslySetInnerHTML={{ __html: lineHtml }}
                    />
                  )
                }
                 
              })
            
            }
          </ol>
        </div>
    </>
  );
}

export default SourceCode;
