import {Fragment, useState, useEffect } from "react"

function IssuesTemplate({ issueDatas }) {
  // console.info("触发issues模块渲染")
  // console.info(issueDatas)
  if(issueDatas==null || Array.from(issueDatas).length==0){
    return null;
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
      </div>
    );
  }


  return issueDatas != null
    ? issueDatas.map((issue) => (
        renderIssue(issue)
      ))
    : null;
}


export default IssuesTemplate;
