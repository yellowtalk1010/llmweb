import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"
import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { Marked } from "marked"; 

// import './resources/float_win.js'

import '../float_window.css'
import '../cpp.css'
// import '../marked.min.js'
// import '../float_window.js'

function renderFinished() {
  console.log("DOM加载完成");

    const buttons = document.querySelectorAll('.btn');
    var websocketResult = null;
    // 创建 WebSocket 连接（仅一个连接复用）
    const socket = new WebSocket("ws://localhost:8080/ws");

    socket.addEventListener('open', () => {
        console.log('✅ WebSocket 连接已打开');
    });

    socket.addEventListener('error', (err) => {
        console.error('❌ WebSocket 连接出错', err);
    });
    
    socket.onmessage = function (event) {
        console.log("收到消息: " + event.data);
        console.log(websocketResult)

        //将数据写入到隐藏输入框中
        document.getElementById("textarea_hidden-" + websocketResult).value
            = document.getElementById("textarea_hidden-" + websocketResult).value + event.data

        const input = document.getElementById("textarea_hidden-" + websocketResult).value;
        const html = Marked.parse(input);
        console.info("html:" + html)
        document.getElementById("result-" + websocketResult).innerHTML = html;
    };

    buttons.forEach((btn, index) => {
        var issueId = btn.id
        // console.info(issueId)
        // 创建对应的悬浮层
        const popup = document.createElement('div');
        popup.className = 'popup';
        popup.innerHTML = `
          <div>AI审计</div>
          <textarea placeholder="请输入..." rows="4" class="textarea"></textarea>
          <br>
          <button class="check-btn">审计</button> 
          <button class="close-btn">关闭</button>
          <br>
          <textarea class="textarea_hidden" id="textarea_hidden-${issueId}" hidden="hidden"></textarea>
          <br>
          <div class="result" id="result-${issueId}"></div>
        `;
        document.body.appendChild(popup);

        const textarea = popup.querySelector('.textarea');
        const checkBtn = popup.querySelector('.check-btn');
        const closeBtn = popup.querySelector('.close-btn');
        const result = popup.querySelector('.result') //全局变量


        // 点击按钮显示悬浮层
        btn.addEventListener('click', () => {
            const rect = btn.getBoundingClientRect();
            popup.style.left = rect.right + 10 + 'px';
            popup.style.top = rect.top + window.scrollY + 'px';
            popup.style.display = 'block';

            // 可选：自动聚焦输入框
            popup.querySelector('input').focus();
        });

        // 点击“关闭”隐藏悬浮层
        closeBtn.addEventListener('click', () => {
            websocketResult = null
            popup.style.display = 'none';
        });

        checkBtn.addEventListener('click', () => {

            websocketResult = issueId
            var code = textarea.value
            const str = issueId + "##########" + code
            console.info(str)
            socket.send(str);

        })


    });
}


function SourceCode() {

  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const vtid = queryParams.get('vtid');
  const file = queryParams.get('file');

  console.info(vtid)
  console.info(file)

  const [sourceCodeData, setSourceCodeData] = useState({
    list:[],
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
        list: data,
        status: 200
      })
      console.info("渲染完成后执行")
      renderFinished()
    }).catch(e =>{
      console.log(e)
    })
  }

  return (
    <>
        <div id="SourceCode">

            <ol>

                {sourceCodeData.list.map((line, index) => (
                    <span
                    key={index}
                    dangerouslySetInnerHTML={{ __html: line }}
                    />
                ))}

                

            </ol>
        </div>
    </>
  );
}

export default SourceCode;
