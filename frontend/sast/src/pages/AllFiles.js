import { useNavigate } from 'react-router-dom';
import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import {Fragment, useState, useEffect, useRef } from "react"
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";

import FileTree from "./modules/FileTree";
import SourceFile from "./modules/SourceFile";

import styles from './AllFiles.css';

function AllFiles() {

  /**
   * 窗口折叠情况
   */
  const leftPanelRef = useRef(null);
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [isHovering, setIsHovering] = useState(false);

  const toggleLeftPanel = (e) => {
    e.stopPropagation();
    if (isCollapsed) {
      leftPanelRef.current.expand();
    } else {
      leftPanelRef.current.collapse();
    }
    setIsCollapsed(!isCollapsed);
  };

  /**
   * 读取url中的参数
   */
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const urlParamVtid = queryParams.get('vtid')==null?"":queryParams.get('vtid');
  const urlParamFile = queryParams.get('file')==null?"":queryParams.get('file');
  console.info("传参，urlParamVtid:" + urlParamVtid)
  console.info("传参，urlParamFile:" + urlParamFile)



  /**
   * 
   */
  const [treeData, setTreeData] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);

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
    <div>
      <div>
        <PanelGroup direction="horizontal" style={{ height: "100vh" }}>
          <Panel
            ref={leftPanelRef}
            collapsible
            defaultSize={10}
            minSize={8}
            style={{ overflow: "auto" }}
          >
            <h3 style={{ marginTop: 0 }}>文件折叠 <font color='red'>{urlParamVtid}</font></h3>
            <FileTree nodes={treeData} onSelectFile={setSelectedFile} />
          </Panel>

          <PanelResizeHandle
            style={{
              width: "12px",
              backgroundColor: isHovering ? "#e5e7eb" : "#f3f4f6",
              position: "relative",
              transition: "background-color 0.2s",
            }}
            onMouseEnter={() => setIsHovering(true)}
            onMouseLeave={() => setIsHovering(false)}
          >
            <div
              onClick={toggleLeftPanel}
              style={{
                position: "absolute",
                left: "50%",
                top: "50%",
                transform: "translate(-50%, -50%)",
                cursor: "pointer",
                padding: "4px",
                borderRadius: "4px",
                backgroundColor: isHovering ? "#d1d5db" : "transparent",
              }}
            >
              {isCollapsed ? "▶️" : "◀️"}
            </div>
          </PanelResizeHandle>

          <Panel defaultSize={90} style={{ overflow: "auto" }}>
            {selectedFile ? (
              <>
                <SourceFile node={selectedFile} urlParamVtid={urlParamVtid}  />
                <pre style={{ whiteSpace: 'pre-wrap' }}>{selectedFile.content}</pre>
              </>
            ) : (
              <p>点击左侧文件查看内容</p>
            )}
          </Panel>
        </PanelGroup>
      </div>

 
      { //如果指定文件，则不平铺
        (urlParamFile==null || urlParamFile=="") && (
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
        </div>)
      }
          
    </div>

  );
}

export default AllFiles;
