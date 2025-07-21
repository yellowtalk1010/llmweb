import {Fragment, useState, useEffect } from "react"

/**
 * @param: issueDatas 是跟随 文件返回 issue列表数据
 */
function IssuesTemplate({ issueDatas, onShowPopup }) {
  // console.info("触发issues模块渲染")
  // console.info(issueDatas)

  if(issueDatas==null || Array.from(issueDatas).length==0){
    return null;
  }


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
      //closeFloatingFile()
      //同一个文件中
      const el = document.getElementById("line_" + trace.line)
      el.scrollIntoView({
        behavior: 'smooth',      // 平滑滚动
        block: 'center',         // 垂直方向：滚动到中间
        inline: 'center'         // 水平方向：滚动到中间
      });

      el.classList.add('sourcecode-li')
      
      console.info("统一文件中打开")
    }
    else{
      //不同文件中打开
      // openFloatingFile(event, trace)
      console.info("不同文件中打开")

      onShowPopup(trace)
    }

    
      
  }

  //渲染issue
  function renderIssue(issue) {
    return (
      <div key={issue.id} id={issue.id} className="issueDiv">
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
              <a className="trace_a" onClick={(event)=>link(event, issue.filePath, trace)} >{trace.file} # {trace.line} # {trace.message}</a>
            </div>
          ))
        }
        </div>
      </div>
    );
  }

  //渲染functonModule函数建模
  function renderFunctionModule(issue) {
    return (
      <div key={issue.id} id={issue.id} className="issueDiv">
        <div>{issue.name}</div>
        <div>
          函数建模
        </div>
      </div>
    );
  }


  return issueDatas != null
    ? issueDatas.map((issue) => {
      if(issue.vtId=="FunctionModule"){
        return renderFunctionModule(issue)
      }
      else{
        return renderIssue(issue)
      }
    })
    : null;
}


export default IssuesTemplate;
