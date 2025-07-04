import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"

import styles from './Files.css';

function Files() {
  const navigate = useNavigate();

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

       
        <ul>
          {filesData.list.map((file, index) => (
          <li key={index}>
            <a href={`file?path=${file.file}`}>{file.file}</a> &nbsp;&nbsp; {file.size}
          </li>
        ))}
        </ul>

    </div>

  );
}

export default Files;
