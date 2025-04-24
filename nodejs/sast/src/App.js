import { 
  // Fragment,
   useState } from "react"

// import axios from 'axios';


function App() {

  const [data, setData] = useState({
    title: '默认标题',
    content: '默认内容'
  })
  function handleClick(event){

    fetch('/get', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    }).then((response) => {
      console.info(response)
    }).catch((error) => {
      console.error(error)
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
