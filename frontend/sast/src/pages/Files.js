import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"

function Files() {
  const navigate = useNavigate();

  // const [rulesData, setRulesData] = useState(null)
  // if(rulesData==null){
  //   fetch('/rules_list', {
  //     method: 'GET',
  //     headers: {
  //       'Content-Type': 'application/json'
  //     }
  //   }).then(res =>{
  //     const json = res.json();
  //     // console.info(json)
  //     return json
  //   }).then(data =>{
  //     console.log("rules_list的数据")
  //     console.log(data)
  //     setIssueFileData('hello')
  //   }).catch(e =>{
  //     console.log(e)
  //   })
  // }

  return (
    <div>
      <h1>文件集</h1>
       
    </div>
  );
}

export default Files;
