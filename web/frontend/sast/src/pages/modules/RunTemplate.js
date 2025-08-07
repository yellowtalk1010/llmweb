import {Fragment, useState, useEffect } from "react"


function RunTemplate() {

    return (
        <form>
            <div>
              <div>
                <button>运行</button>
              </div>
              <div>
                <span>命令:</span>
              </div>
              <input type='text'></input>
            </div>
            <div>
              <div>
                <span>参数:</span>
              </div>
              <textarea></textarea>
            </div>
          </form>
    );
}

export default RunTemplate;