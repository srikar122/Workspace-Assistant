import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import ChatContainer from '../components/chat/ChatContainer';

describe('ChatContainer Component', () => {
  it('submits user prompt and clears the input field', () => {
    const mockSend = vi.fn();
    render(<ChatContainer messages={[]} isSending={false} onSendPrompt={mockSend} />);

    const input = screen.getByLabelText('Agent prompt input');
    fireEvent.change(input, { target: { value: 'Reschedule my 3 PM call' } });
    fireEvent.submit(input.closest('form'));

    expect(mockSend).toHaveBeenCalledWith('Reschedule my 3 PM call');
    expect(input.value).toBe('');
  });
});