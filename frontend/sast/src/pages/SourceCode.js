'use client';
import { data, useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"
import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { Marked } from "marked"; 
import React, { useRef } from 'react';
import { Rnd } from 'react-rnd';
 
import '../float_window.css'
import '../float_file.css'
import '../cpp.css'

function SourceCode() {

  var aiCheckIssueID = null //保存当前打开的 ai check 交互界面
  var socket = null;
  if(socket==null){
    socket = new WebSocket("ws://localhost:8080/ws");

    socket.addEventListener('open', () => {
        console.log('✅ WebSocket 连接已打开');
    });

    socket.addEventListener('error', (err) => {
        console.error('❌ WebSocket 连接出错', err);
    });

    socket.onmessage = function (event) {
      console.log("收到消息: " + event.data);
      //将数据写入到隐藏输入框中
      document.getElementById("textarea_hidden_" + aiCheckIssueID).value
          = document.getElementById("textarea_hidden_" + aiCheckIssueID).value + event.data
      const input = document.getElementById("textarea_hidden_" + aiCheckIssueID).value;
      const markedInstance = new Marked();
      const html = markedInstance.parse(input);
      console.info("html:" + html)
      document.getElementById("result_" + aiCheckIssueID).innerHTML = html;
    };
  }

  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const vtid = queryParams.get('vtid');
  const file = queryParams.get('file');

  // console.info(vtid)
  // console.info(file)

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
      // console.log("rule_vtid 的数据")
      // console.log(data)
      setSourceCodeData({
        lines: data.lines,
        issues: data.issues,
        status: 200
      })
      // console.info("渲染完成后执行")
    }).catch(e =>{
      console.log(e)
    })
    
  }
  
  //打开悬浮窗口
  function openAiCheck(event) {
    console.info("打开悬浮窗口")
    const id = event.target.id
    console.info(id)
    var aiCheckDiv = document.getElementById("aiCheckId_" + id)
    console.info(aiCheckDiv)

    console.info("修改前class的值:" + aiCheckDiv.className)
    aiCheckDiv.className = 'aiCheck aiCheckShow';
    console.info("修改后class的值:" + aiCheckDiv.className)
  }

  //关闭悬浮窗口
  function closeAiCheck(event){
    aiCheckIssueID = null
    console.info("关闭悬浮窗口")
    const closeBut = event.target
    console.info(closeBut)

    const aiCheckDiv = closeBut.closest('div[class*="aiCheck"]');
    console.info(aiCheckDiv)
    console.info("修改前class的值:" + aiCheckDiv.className)
    aiCheckDiv.className = "aiCheck aiCheckHidden"
    console.info("修改后class的值:" + aiCheckDiv.className)
  }

  //执行AI check
  function aiCheck(id) {

    document.getElementById("textarea_hidden_" + id).value = ""
    document.getElementById("result_" + id).innerHTML = ""

    console.info(id)
    //获取多文本输入的内容部
    const textareaDom = document.getElementById("textarea_" + id)
    console.info(textareaDom)
    const code = textareaDom.value
    if(code==null || code.trim().length == 0){
      console.info(document.getElementById("result_" + id))
      document.getElementById("textarea_hidden_" + id).value = "没有输入数据"
      document.getElementById("result_" + id).innerHTML = "没有输入数据"
    }
    else{
      aiCheckIssueID = id //保存当前打开的 ai check 交互界面
      const json = {
        issueId: id,
        content: code
      }
      const jsonString = JSON.stringify(json, null, 2);
      console.info(jsonString)
      socket.send(jsonString);
    }
    
  }

  //点击 trace a 标签
  const [top, setTop] = useState(0);
  function link(event, currentFile, trace){
    console.info(trace)
    document.getElementById("sourcecode_file").querySelectorAll('div a.trace_a_click').forEach(li => {
      li.classList.remove('trace_a_click');
    });
    document.getElementById("sourcecode_file").querySelectorAll('div li.sourcecode-li').forEach(li => {
      li.classList.remove('sourcecode-li');
    });

    event.target.classList.add('trace_a_click')

    const toId = trace.id
    const toFile = trace.file
    if(currentFile==toFile){
      closeFloatingFile()
      //同一个文件中
      const el = document.getElementById("line_" + trace.line)
      el.scrollIntoView({
        behavior: 'smooth',      // 平滑滚动
        block: 'center',         // 垂直方向：滚动到中间
        inline: 'center'         // 水平方向：滚动到中间
      });

      // el.classList.add('flash-border');
      // // 两秒后移除动画 class（避免永久保留）
      // setTimeout(() => {
      //   el.classList.remove('flash-border');
      // }, 6000);

      el.classList.add('sourcecode-li')


    }
    else{
      //不同文件中打开
      openFloatingFile(event, trace)
    }

    
      
  }

  const [otherSourceCodeData, setOtherSourceCodeData] = useState({
    lines:[]
  })
  function openFloatingFile(event, trace){
    const file = trace.file
    //打开一个新文件，展示在div中
    const filePath = document.getElementById("floating-file-name").textContent
    console.info("filePath:" + filePath)
    if(filePath=="" || filePath!=file){
      setOtherSourceCodeData({
        lines: [],
        status: 200
      })
      document.getElementById("floating-file-name").innerText = file
      //加载源文件
      fetch('/otherSourceCode_list?file='+file, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(res =>{
        const json = res.json();
        // console.info(json)
        return json
      }).then(data =>{
        // console.log("rule_vtid 的数据")
        console.log(data)
        setOtherSourceCodeData({
          lines: data,
          status: 200
        })
        // console.info("渲染完成后执行")
      }).catch(e =>{
        console.log(e)
      })
    }

    const rect = event.target.getBoundingClientRect();
    setTop(rect.bottom + window.scrollY + 5); // 按钮下方 +10px

    var floatingFileDom = document.getElementById("floating-file")
    console.info(floatingFileDom)
    floatingFileDom.classList.remove("floating-file-hidden")
    floatingFileDom.classList.add("floating-file-show")

    console.info(document.getElementById("otherfileline_26"))
    const otherFileLiDom = document.getElementById("otherfileline_" + trace.line)
    if(otherFileLiDom){
      const containerHeight = floatingFileDom.clientHeight;
      const liOffsetTop = otherFileLiDom.offsetTop;
      const liHeight = otherFileLiDom.offsetHeight;
      // 计算 li 要滚动到的位置，使其垂直居中
      const scrollTop = liOffsetTop - (containerHeight / 2) + (liHeight / 2);
      // 平滑滚动到目标位置
      floatingFileDom.scrollTo({
        top: scrollTop,
        behavior: 'smooth'
      });

      //将li标签加粗
      otherFileLiDom.classList.add('sourcecode-li');
 
    }

  }

  function closeFloatingFile(event){
    var floatingFileDom = document.getElementById("floating-file")
    floatingFileDom.classList.remove("floating-file-show")
    floatingFileDom.classList.add("floating-file-hidden")

    //将div下面所有含 sourcecode-li class 的全部移除
    const liList = floatingFileDom.querySelectorAll('div li.sourcecode-li');
    // 遍历并删除 class 中的 "sourcecode-li"
    liList.forEach(li => {
      li.classList.remove('sourcecode-li');
    }); 
  }

  //渲染issue列表
  function renderIssue1(lineIssues) {
    return lineIssues.map((issue, index) => (
      <div id={issue.id} class="floatDiv">
        <div>{issue.name}</div>
        <div>
          {issue.line}/{issue.vtId}/{issue.rule}/{issue.defectLevel}/{issue.defectType}
        </div>
        <div>{issue.ruleDesc}</div>
        <div>
        {
          issue.traces.map((trace, traceIndex) => (
            <div>
              <a className="trace_a" onClick={(event)=>link(event, issue.filePath, trace)} >{trace.file} # {trace.line} # {trace.message}</a>
            </div>
          ))
        }
        </div>
        <a class="btn" onClick={openAiCheck} id={issue.id}>AI审计</a>
        
        {/* 添加人工AI check交互框 */}
        <div id={"aiCheckId_" + issue.id} class="aiCheck aiCheckHidden">
          <textarea placeholder="请输入..." rows="4" class="textarea" id={"textarea_" + issue.id}></textarea>
          <br></br>
          <button class="check-btn" onClick={()=>aiCheck(issue.id)}>审计</button> 
          <button class="close-btn" onClick={closeAiCheck}>关闭</button>
          <br></br>

          {/* 隐藏的 textarea 用来转markdown格式 */}
          <textarea class="textarea" id={"textarea_hidden_" + issue.id} hidden="hidden"></textarea>
          <br></br>
          <div class="result" id={"result_" + issue.id}></div>
        </div>

      </div>
    ));
  }
  
  return (
    <>
        <div id="SourceCode">
          <div id="sourcecode_file">
            <ol>
              {
                sourceCodeData.lines.map((lineHtml, index) => {
                  const lineNumber = index + 1;
                  const lineIssues = sourceCodeData.issues.filter(issue=>issue.line==lineNumber) || []

                  const newLiElement = (
                    <span
                    key={index}
                    dangerouslySetInnerHTML={{ __html: lineHtml }} //将字符串转成 react元素
                    />
                  );

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
                    return newLiElement
                  }
                  
                })
              
              }
            </ol>
          </div>

          {(
            <div id="floating-file" className="floating-file floating-file-hidden" style={{ top: `${top}px` }}>

              <button className="floating-file-close-btn" onClick={closeFloatingFile}>X</button>
              <div className="floating-file-file-btn" id="floating-file-name">文件名</div> 
              <ol className="floating-file-content">
                {
                  otherSourceCodeData.lines.map((lineHtml, index) => {
                    const newLiElement = (
                      <span
                      key={index}
                      dangerouslySetInnerHTML={{ __html: lineHtml }} //将字符串转成 react元素
                      />
                    );
                    return newLiElement
                  })
                }
              </ol>
            </div>
          )}

        </div>
    </>
  );
}

export default SourceCode;
