import { useNavigate } from 'react-router-dom';
import {Fragment, useState } from "react"
import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import { useEffect } from 'react';

import '../float_window.css'
import '../cpp.css'
import '../marked.min.js'
import '../float_window.js'
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

  const [sourceCodeData, setSourceCodeData] = useState({
    list:[],
    status: 0
  })
  if(sourceCodeData.status==0){
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
      setSourceCodeData({
        list: data,
        status: 200
      })
    }).catch(e =>{
      console.log(e)
    })
  }

  return (
    <>
        <div id="SourceCode">
            <ol>

                {sourceCodeData.list.map((line, index) => (
                    <li
                    key={index}
                    dangerouslySetInnerHTML={{ __html: line }}
                    />
                ))}

                

            </ol>
        </div>
    </>
  );
}

export default SourceCode;
