import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"

function IssueResultFile() {
  const navigate = useNavigate();

  //获取上传的issue文件信息
  const [issueFileData, setIssueFileData] = useState({
    issueResultFilePath: '', //加载的issue文件路径
    issueNum: '', //issue个数
    rulesPage: '', //规则集跳转
    filesPage: '' //文件集跳转
  })

  function loadIssue() {
    console.info("loadIssue")
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
      console.log(data)
      setIssueFileData({
        ...data,
        rulesPage: <a href='/pages/Rules'>规则集</a>,
        filesPage: <a href='/pages/Files'>文件集</a>
      })
    }).catch(e =>{
      console.log(e)
    })
  }

  //文件上传功能
  const [file, setFile] = useState(null);
  const [message, setMessage] = useState("");

  const onFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  const onUpload = async () => {
    if (!file) {
      setMessage("请先选择文件！");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch("/upload", {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        const text = await response.text();
        setMessage(text);

        loadIssue();
      } else {
        setMessage("上传失败！");
      }
    } catch (error) {
      console.error(error);
      setMessage("上传出错！");
    }
  };

  return (
    
    <div class='IssueResultFile'>

      <div className="upload">
        <input type="file" onChange={onFileChange} className="mb-4" />
        <button onClick={onUpload} className="bg-blue-500 text-white px-4 py-2 rounded">上传</button>
        <div className="mt-4">{message}</div>
      </div>

      <hr></hr>

      <div>
        {issueFileData.issueResultFilePath}
        <br></br>
        issue 总数：{issueFileData.issueNum}
        <br></br>
        {issueFileData.filesPage}
        <br></br>
        {issueFileData.rulesPage}
      </div>

      
    </div>
    
  );
}

export default IssueResultFile;