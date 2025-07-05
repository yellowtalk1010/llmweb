import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"
import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';

function Start() {

  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const token = queryParams.get('token');

  console.info(token)

  // const [fileData, setFileData] = useState({
  //   list: [],
  //   path: '',
  //   status: 0
  // }) 

  // if(fileData.status==0){
  //   fetch('/file_path?path='+path, {
  //     method: 'GET',
  //     headers: {
  //       'Content-Type': 'application/json'
  //     }
  //   }).then(res =>{
  //     const json = res.json();
  //     // console.info(json)
  //     return json
  //   }).then(data =>{
  //     console.log("file_path 的数据")
  //     console.log(data)
  //     setFileData({
  //       ...data,
  //       status: 200
  //     })
  //   }).catch(e =>{
  //     console.log(e)
  //   })
  // }

  return (
    <div id="start">
      <form action='aaavvv'>
        <div>
          项目名称: <input type='text' name='projectName'></input>
        </div>
        <div>
          规则类型: <input type='text' name="type"></input>
        </div>
        <div>
          扫描路径: <input type='text' name="scan"></input>
        </div>
        <div>
          <button type='submit'>运行</button>
        </div>

                


      </form>

      {/* <table style={{ width: '100%', borderCollapse: 'collapse', tableLayout: 'auto' }}>
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
      </table> */}

    </div>
  );
}

export default Start;
