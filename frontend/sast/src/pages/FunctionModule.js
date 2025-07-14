'use client';
import { data, useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"
import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { Marked } from "marked"; 
import React, { useRef } from 'react';
 
import '../float_window.css'
import '../cpp.css'

function FunctionModule() {

  var aiCheckIssueID = null //保存当前打开的 ai check 交互界面
   

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

   //渲染issue列表
  function renderIssue1(lineIssues) {
    return lineIssues.map((issue, index) => (
      <div id={issue.id} class="issueDiv">
        <div>{issue.name}</div>
        <div>
          {issue.line}/{issue.vtId}/{issue.rule}/{issue.defectLevel}/{issue.defectType}
        </div>
        <div>{issue.ruleDesc}</div>
        <div>{issue.issueDesc}</div>
        <div>
        {
          issue.traces.map((trace, traceIndex) => (
            <div>
              <a className="trace_a">{trace.file} # {trace.line} # {trace.message}</a>
            </div>
          ))
        }
        </div>

      </div>
    ));
  }
   
  
  
  return (
    <>
        <div id="FunctionModule">
          <div id="functionmodule_file">
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

        </div>
    </>
  );
}

export default FunctionModule;
