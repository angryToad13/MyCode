import { Injectable, NgZone } from '@angular/core';
import { Observable, Subject, fromEvent } from 'rxjs';
import { filter, map, takeUntil } from 'rxjs/operators';

export interface BroadcastMessage<T = any> {
  type: string;
  data: T;
}

/**
 * A singleton service for cross-tab/window communication using the BroadcastChannel API
 * with performance optimizations through NgZone.
 */
@Injectable({
  providedIn: 'root' // Singleton service provided at root level
})
export class BroadcastChannelService {
  private channel: BroadcastChannel | null = null;
  private channelName = 'app-broadcast-channel';
  private destroyed$ = new Subject<void>();
  private messageSubject = new Subject<BroadcastMessage>();

  /**
   * Message Observable that runs outside NgZone for better performance
   */
  public readonly message$: Observable<BroadcastMessage> = this.messageSubject.asObservable();

  constructor(private ngZone: NgZone) {
    // Check if BroadcastChannel API is supported
    if (typeof BroadcastChannel !== 'undefined') {
      this.initChannel();
    } else {
      console.warn('BroadcastChannel API is not supported in this browser');
    }
  }

  /**
   * Initialize the BroadcastChannel
   */
  private initChannel(): void {
    // Run channel creation outside NgZone to avoid unnecessary change detection
    this.ngZone.runOutsideAngular(() => {
      this.channel = new BroadcastChannel(this.channelName);
      
      // Listen for messages outside NgZone
      fromEvent<MessageEvent>(this.channel, 'message')
        .pipe(takeUntil(this.destroyed$))
        .subscribe(event => {
          // Only run change detection if needed by re-entering NgZone
          this.ngZone.run(() => {
            this.messageSubject.next(event.data);
          });
        });
    });
  }

  /**
   * Post a message to all other tabs/windows
   * @param type Message type identifier
   * @param data The payload to broadcast
   */
  public postMessage<T>(type: string, data: T): void {
    if (!this.channel) {
      console.warn('BroadcastChannel is not available');
      return;
    }

    const message: BroadcastMessage<T> = { type, data };
    
    // Run outside NgZone to avoid triggering change detection
    this.ngZone.runOutsideAngular(() => {
      this.channel!.postMessage(message);
    });
  }

  /**
   * Get an observable that emits only messages of a specific type
   * @param type The message type to filter by
   * @returns Observable of the filtered messages
   */
  public on<T>(type: string): Observable<T> {
    return this.message$.pipe(
      filter((message: BroadcastMessage) => message.type === type),
      map((message: BroadcastMessage) => message.data as T)
    );
  }

  /**
   * Clean up resources when the service is destroyed
   */
  public ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
    
    if (this.channel) {
      this.channel.close();
      this.channel = null;
    }
  }
}




#eg

// example.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { BroadcastChannelService } from './broadcast-channel.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-broadcast-example',
  template: `
    <div class="container">
      <h2>BroadcastChannel Demo</h2>
      <p>Tab ID: {{ tabId }}</p>
      
      <div class="form-group">
        <input 
          type="text" 
          [(ngModel)]="messageText" 
          placeholder="Enter a message"
          class="form-control">
        <button 
          (click)="sendMessage()" 
          class="btn btn-primary">
          Send to All Tabs
        </button>
      </div>
      
      <div class="messages">
        <h3>Received Messages:</h3>
        <ul>
          <li *ngFor="let msg of receivedMessages">
            {{ msg.tabId }}: {{ msg.text }}
          </li>
        </ul>
      </div>
    </div>
  `
})
export class BroadcastExampleComponent implements OnInit, OnDestroy {
  public tabId = `Tab-${Math.floor(Math.random() * 10000)}`;
  public messageText = '';
  public receivedMessages: Array<{ tabId: string, text: string }> = [];
  
  private destroy$ = new Subject<void>();

  constructor(private broadcastService: BroadcastChannelService) {}

  ngOnInit(): void {
    // Subscribe to chat messages
    this.broadcastService.on<{ tabId: string, text: string }>('CHAT_MESSAGE')
      .pipe(takeUntil(this.destroy$))
      .subscribe(message => {
        this.receivedMessages.push(message);
      });
      
    // Subscribe to notifications
    this.broadcastService.on<string>('NOTIFICATION')
      .pipe(takeUntil(this.destroy$))
      .subscribe(notification => {
        console.log('Notification received:', notification);
        // Handle notifications (e.g., show toast)
      });
      
    // Announce this tab is now open
    this.broadcastService.postMessage('TAB_OPENED', { 
      tabId: this.tabId, 
      timestamp: new Date().toISOString() 
    });
  }

  sendMessage(): void {
    if (!this.messageText.trim()) {
      return;
    }
    
    // Send the message to all tabs
    this.broadcastService.postMessage('CHAT_MESSAGE', {
      tabId: this.tabId,
      text: this.messageText.trim()
    });
    
    // Add the message to our own list as well
    this.receivedMessages.push({
      tabId: `${this.tabId} (You)`,
      text: this.messageText.trim()
    });
    
    // Clear the input
    this.messageText = '';
  }

  ngOnDestroy(): void {
    // Clean up subscriptions
    this.destroy$.next();
    this.destroy$.complete();
    
    // Notify other tabs that this tab is closing
    this.broadcastService.postMessage('TAB_CLOSED', {
      tabId: this.tabId,
      timestamp: new Date().toISOString()
    });
  }
}
