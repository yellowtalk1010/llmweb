import { useNavigate } from 'react-router-dom';
import {Fragment, useState, useEffect } from "react"
import FileTree from "./modules/FileTree";

import styles from './AllFiles.css';

function AllFiles() {
  const navigate = useNavigate();

  const [treeData, setTreeData] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);

  useEffect(() => {
    fetch('/file_tree', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
      .then(res => {
        const json = res.json()
        return json
      })
      .then(data => {
        console.log("file_tree的数据")
        console.log(data)
        setTreeData(data)
      }).catch(e=>{
        console.error(e)
      });
  }, []);


  const [filesData, setFilesData] = useState({list:[], status: 0})
  if(filesData.status==0){
    fetch('/file_list', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res =>{
      const json = res.json();
      // console.info(json)
      return json
    }).then(data =>{
      console.log("files_list的数据")
      console.log(data)
      setFilesData({
        list: data,
        status: 200
      })
    }).catch(e =>{
      console.log(e)
    })
  }

  return (
    <div id="files">

      <div style={{ display: 'flex', height: '100vh', fontFamily: 'sans-serif' }}>
        <div style={{ width: '300px', borderRight: '1px solid #ccc', padding: '1rem', overflowY: 'auto' }}>
          <h3>📁 文件树</h3>
          <FileTree nodes={treeData} onSelectFile={setSelectedFile} />
        </div>
        <div style={{ flex: 1, padding: '1rem', overflowY: 'auto' }}>
          <h3>📄 文件内容</h3>
          {selectedFile ? (
            <pre style={{ whiteSpace: 'pre-wrap' }}>{selectedFile.content}</pre>
          ) : (
            <p>点击左侧文件查看内容</p>
          )}
        </div>
      </div>
      

      <div>
        <ul>
          {filesData.list.map((file, index) => (
          <li key={index}>
            <a href={`file?path=${file.file}`}>{file.file}</a> &nbsp;&nbsp; {file.size}
          </li>
        ))}
        </ul>
      </div>    
    </div>

  );
}

export default AllFiles;
