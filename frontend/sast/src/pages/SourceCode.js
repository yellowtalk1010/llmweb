'use client';
import { data, useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"
import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { Marked } from "marked"; 
import React, { useRef } from 'react';
import { ArcherContainer, ArcherElement } from 'react-archer'; //用来自动化划线
 
import '../float_window.css'
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

  //自动装载划线
  const [currentRelation, setCurrentRelation] = useState([]);
  const generateRelations = (id) => {
    currentRelation.forEach(item=>{
      console.info(">>>>" + item)
    })
    const relations = [];
    if(currentRelation.length>1){
      relations.push({
        sourceId: currentRelation[0],
        targetId: currentRelation[1],
        sourceAnchor: 'bottom',
        targetAnchor: 'bottom',
        curve: 0.5
      });
    }

    return relations
  }

  //触发划线功能
  function link(fromId, toId, filePath){
    if(fromId!=null && toId!=null && fromId!=toId){
      var array = []
      setCurrentRelation(array)
      array.push(fromId)
      array.push(toId)
      setCurrentRelation(array)

    }
  }

  //渲染issue列表
  function renderIssue1(lineIssues) {
    return lineIssues.map((issue, index) => (
      <div
        key={issue.id}
        class="floatDiv"
      >
        <div>
        <ArcherElement key={issue.id} id={issue.id} relations={generateRelations(issue.id)}>
          <span class="floatDiv">{issue.name}</span>
        </ArcherElement>
        </div>
        <div>
          {issue.line}/{issue.vtId}/{issue.rule}/{issue.defectLevel}/{issue.defectType}
        </div>
        <div>{issue.ruleDesc}</div>
        <div>
        {
          issue.traces.map((trace, traceIndex) => (
            <div>
              <a href="#" onClick={()=>link(issue.id, trace.id, trace.file)} >{trace.file} # {trace.line} # {trace.message}</a>
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
      <ArcherContainer strokeColor="red" strokeWidth={3} style={{ height: '100%' }} >
        <div id="SourceCode">
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
      </ArcherContainer>  
    </>
  );
}

export default SourceCode;
