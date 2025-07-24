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
      console.info("点击文件树中的文件:")
      console.info(node)
      onSelectFile(node);
    }
  };

  const handleCopy = (e) => {
    e.stopPropagation(); // 避免触发点击展开或选中文件
    navigator.clipboard.writeText(node.name)
      .then(() => {
        alert(`已复制文件名: ${node.name}`);
      })
      .catch((err) => {
        console.error('复制失败', err);
      });
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

        {!isFolder && (
          <span
            title="复制文件名"
            onClick={handleCopy}
            style={{
              marginLeft: '0.5rem',
              cursor: 'pointer',
              color: '#666',
              fontSize: '0.9rem',
            }}
          >
            📋
          </span>
        )}
        
      </div>
      {isFolder && expanded && (
        <FileTree nodes={node.children} onSelectFile={onSelectFile} />
      )}
    </li>
  );
}

export default FileTree;
