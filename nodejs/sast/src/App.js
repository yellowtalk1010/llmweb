import {Fragment, useState } from "react"

function App() {

  const [data, setData] = useState({
    title: '默认标题',
    content: '默认内容'
  })
  function handleClick(event){

    fetch('/get1', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(res =>{
      // console.info(res.json)
      return res.json();
  }).then(data =>{
      console.log("get请求的数据")
      console.log(data)
      console.log(data.name)
  }).catch(e =>{
      console.log(e)
  })
 

    console.info(event)

    setData({
      ...data,
      title: "新标题"
    })
  }

  

  return (
    <div className="App" >
      
      {data.title}-{data.content}
      <button onClick={handleClick}>按钮</button>
    </div>
  );
}

export default App;
