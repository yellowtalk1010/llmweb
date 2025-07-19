import {Fragment, useState, useEffect } from "react"

function SourceFile({node}) {
    return (
        <>
        <span>{node.path}</span>
        
        <div>展示源代码</div>
        </>
    );
}


export default SourceFile;