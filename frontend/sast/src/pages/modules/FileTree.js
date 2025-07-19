import {Fragment, useState } from "react"

function FileTree({ nodes, onSelectFile }) {
  return (
    <ul style={{ listStyleType: 'none', paddingLeft: '1rem' }}>
      {nodes.map((node, index) => (
        <TreeNode key={index} node={node} onSelectFile={onSelectFile} />
      ))}
    </ul>
  );
}

function TreeNode({ node, onSelectFile }) {
  const [expanded, setExpanded] = useState(false);
  const isFolder = Array.isArray(node.children) && Array.from(node.children).length > 0;  //如果子孩子数量大于1就是文件夹

  const handleClick = () => {
    if (isFolder) {
      setExpanded(!expanded);
    } else {
      onSelectFile(node);
    }
  };

  return (
    <li>
      <div
        onClick={handleClick}
        style={{
          cursor: 'pointer',
          fontWeight: isFolder ? 'bold' : 'normal',
          userSelect: 'none',
        }}
      >
        {isFolder ? (expanded ? '📂' : '📁') : '📄'} {node.name}
      </div>
      {isFolder && expanded && (
        <FileTree nodes={node.children} onSelectFile={onSelectFile} />
      )}
    </li>
  );
}

export default FileTree;
