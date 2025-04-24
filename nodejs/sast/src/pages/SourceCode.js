import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"
import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import { useEffect } from 'react';


import '../cpp.css'
import '../marked.min.js'
// import './resources/float_window.js'
// import '../../public/resources/cpp.css'
// import './resources/marked.min.js'

function SourceCode() {

  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const vtid = queryParams.get('vtid');
  const file = queryParams.get('file');

  console.info(vtid)
  console.info(file)

  const [ruleData, setRuleData] = useState({
    list:[],
    defectLevel: '',
    ruleDesc: '',
    status: 0
  })
  if(ruleData.status==0){
    fetch('/sourceCode_list?vtid='+vtid + '&file='+file, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res =>{
      const json = res.json();
      // console.info(json)
      return json
    }).then(data =>{
      console.log("rule_vtid 的数据")
      console.log(data)
      setRuleData({
        ...data,
        status: 200
      })
    }).catch(e =>{
      console.log(e)
    })
  }

  return (
    <>
        <div id="SourceCode">
        
            hello world.
            <br></br>
            {vtid}
            <br></br>
            {file}

        </div>
    </>
  );
}

export default SourceCode;
