import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"

function MymainPage() {
  const navigate = useNavigate();

  const [issueFileData, setIssueFileData] = useState({
    issueResultFilePath: 'loading', //加载的issue文件路径
    issueNum: '', //issue个数
    rulesPage: '', //规则集跳转
    filesPage: '' //文件集跳转
  })

  if(issueFileData.issueNum==''){
    fetch('/getIssueResult', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res =>{
      const json = res.json();
      // console.info(json)
      return json
    }).then(data =>{
      console.log("getIssueResult的数据")
      console.log(data.issueResultFilePath)
      console.log(data.issueNum)
      setIssueFileData({
        ...data,
        rulesPage: <a href='/pages/Page2'>规则集</a>,
        filesPage: <a href='/pages/Page1'>文件集</a>
      })
    }).catch(e =>{
      console.log(e)
    })
  }
  

  return (
    <div class='MymainPage'>
      {issueFileData.issueResultFilePath}
      <br></br>
        issue 总数：{issueFileData.issueNum}
        <br></br>
        {rulesPage}
        <br></br>
        {rulesPage}
    </div>
  );
}

export default MymainPage;