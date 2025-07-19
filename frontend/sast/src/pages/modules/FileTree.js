import {Fragment, useState } from "react"

function FileTree({ nodes, onSelectFile }) {
  return (
    <ul style={{ listStyleType: 'none', paddingLeft: '1rem' }}>
      {nodes.map((node, index) => (
        <li key={index}>
          <span
            style={{ cursor: node.children ? 'default' : 'pointer', fontWeight: node.children ? 'bold' : 'normal' }}
            onClick={() => !node.children && onSelectFile(node)}
          >
            {node.name}
          </span>
          {node.children && (
            <FileTree nodes={node.children} onSelectFile={onSelectFile} />
          )}
        </li>
      ))}
    </ul>
  );
}

export default FileTree;
