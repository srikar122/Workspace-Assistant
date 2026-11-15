import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import TaskSidebar from '../components/tasks/TaskSidebar';

describe('TaskSidebar Component', () => {
  it('renders empty state when no tasks are provided', () => {
    render(<TaskSidebar tasks={[]} />);
    expect(screen.getByText(/No pending tasks/i)).toBeInTheDocument();
  });

  it('renders tasks and triggers onCompleteTask when a pending task is clicked', () => {
    const mockComplete = vi.fn();
    const sampleTasks = [
      { id: 101, title: 'Send slides to Rahul', status: 'PENDING' },
      { id: 102, title: 'Book flight', status: 'COMPLETED' },
    ];

    render(<TaskSidebar tasks={sampleTasks} onCompleteTask={mockComplete} />);

    expect(screen.getByText('Send slides to Rahul')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Send slides to Rahul'));

    expect(mockComplete).toHaveBeenCalledTimes(1);
    expect(mockComplete).toHaveBeenCalledWith(101);
  });
});