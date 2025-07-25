import React, { useEffect } from 'react';
import ReactFlow, { useNodesState, useEdgesState } from 'reactflow';
import 'reactflow/dist/style.css';

// 初始函数调用数据
const initialNodes = [
  { id: 'main', data: { label: 'main()' }, position: { x: 0, y: 0 } },
  { id: 'funcA', data: { label: 'funcA()' }, position: { x: 0, y: 0 } },
  { id: 'funcB', data: { label: 'funcB()' }, position: { x: 0, y: 0 } },
  { id: 'funcC', data: { label: 'funcC()' }, position: { x: 0, y: 0 } },
  { id: 'funcD', data: { label: 'funcD()' }, position: { x: 0, y: 0 } },
];

const initialEdges = [
  { id: 'main-funcA', source: 'main', target: 'funcA' },
  { id: 'main-funcB', source: 'main', target: 'funcB' },
  { id: 'funcA-funcC', source: 'funcA', target: 'funcC' },
  { id: 'funcB-funcD', source: 'funcB', target: 'funcD' },
];

const TreeFlow = () => {
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  // 树状布局调整
  useEffect(() => {
    const horizontalSpacing = 200;
    const verticalSpacing = 100;
    
    setNodes([
      // 第一层 (root)
      { id: 'main', data: { label: 'main()' }, position: { x: 400, y: 50 } },
      
      // 第二层
      { id: 'funcA', data: { label: 'funcA()' }, position: { x: 300, y: 50 + verticalSpacing } },
      { id: 'funcB', data: { label: 'funcB()' }, position: { x: 500, y: 50 + verticalSpacing } },
      
      // 第三层
      { id: 'funcC', data: { label: 'funcC()' }, position: { x: 300, y: 50 + verticalSpacing * 2 } },
      { id: 'funcD', data: { label: 'funcD()' }, position: { x: 500, y: 50 + verticalSpacing * 2 } },
    ]);
  }, []);

  return (
    <div style={{ 
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      width: '100vw',
      height: '100vh'
    }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        fitView
      />
    </div>
  );
};

export default TreeFlow;