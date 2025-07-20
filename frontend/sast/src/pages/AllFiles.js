import { useNavigate } from 'react-router-dom';
import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import {Fragment, useState, useEffect } from "react"
import FileTree from "./modules/FileTree";
import SourceFile from "./modules/SourceFile";

import styles from './AllFiles.css';

function AllFiles() {

  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const urlParamVtid = queryParams.get('vtid')==null?"":queryParams.get('vtid');
  const urlParamFile = queryParams.get('file')==null?"":queryParams.get('file');
  console.info("传参，urlParamVtid:" + urlParamVtid)
  console.info("传参，urlParamFile:" + urlParamFile)

  const navigate = useNavigate();

  const [treeData, setTreeData] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  useEffect(() => {
    fetch('/file_tree?vtid=' + urlParamVtid + "&file=" + urlParamFile, {
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

  /**平铺所有文件**/
  const [filesData, setFilesData] = useState({list:[], status: 0})
  if(filesData.status==0){
    fetch('/file_list?vtid=' + urlParamVtid + "&file=" + urlParamFile, {
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
        {/* 左侧文件树 */}
        <div
          style={{
            width: sidebarCollapsed ? '0' : '300px',
            borderRight: '1px solid #ccc',
            padding: sidebarCollapsed ? '0' : '1rem',
            overflowY: 'auto',
            transition: 'width 0.3s ease',
            whiteSpace: 'nowrap'
          }}
        >
          {!sidebarCollapsed && (
            <>
              <h3 style={{ marginTop: 0 }}>文件折叠 <font color='red'>{urlParamVtid}</font></h3>
              <FileTree nodes={treeData} onSelectFile={setSelectedFile} />
            </>
          )}
          
        </div>

      {/* 折叠/展开按钮 */}
      <div style={{ width: '30px', textAlign: 'center', background: '#f0f0f0', cursor: 'pointer' }}
           onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
      >
        <div style={{ padding: '0.1 rem' }}>
          {sidebarCollapsed ? '▶️' : '◀️'}
        </div>
      </div>

        {/* 展示文件内容 */}    
        <div style={{ flex: 1, padding: '0.1rem', overflowY: 'auto' }}>
          {selectedFile ? (
            <>
            <SourceFile node={selectedFile} urlParamVtid={urlParamVtid}  />
            <pre style={{ whiteSpace: 'pre-wrap' }}>{selectedFile.content}</pre>
            </>
          ) : (
            <p>点击左侧文件查看内容</p>
          )}
        </div>
      </div>
      
      

      <div>
        <hr></hr>
        <ul>
          <h2>文件平铺</h2>
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
