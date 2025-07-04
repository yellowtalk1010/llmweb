import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"
import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';

function File() {

  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const path = queryParams.get('path');

  console.info(path)

  const [fileData, setFileData] = useState({
    // size: '',
    // vtid: '',
    // rule: '',
    // defectLevel: '',
    // ruleDesc: '',
    list: [],
    path: '',
    status: 0
  }) 

  if(fileData.status==0){
    fetch('/file_path?path='+path, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res =>{
      const json = res.json();
      // console.info(json)
      return json
    }).then(data =>{
      console.log("file_path 的数据")
      console.log(data)
      setFileData({
        ...data,
        status: 200
      })
    }).catch(e =>{
      console.log(e)
    })
  }

  return (
    <div id="file">
      <ul>
        <li>{fileData.path}</li>
      </ul>

      <table style={{ width: '100%', borderCollapse: 'collapse', tableLayout: 'auto' }}>
        <tbody>
          {fileData.list.map((rule, index) => (
          <tr key={index}>
            <td><a style={{ textDecoration: 'none' }} href={`sourceCode?vtid=${rule.vtid}&file=${fileData.path}`}>{rule.vtid}</a></td>
            <td>{rule.rule}</td>
            <td>{rule.size}</td>
            <td>{rule.defectLevel}</td>
            <td>{rule.ruleDesc}</td>
          </tr>
        ))}
        </tbody>
      </table>

    </div>
  );
}

export default File;
