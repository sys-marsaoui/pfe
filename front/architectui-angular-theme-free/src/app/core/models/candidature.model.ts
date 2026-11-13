// candidature.model.ts
export interface Role {
  id: number;
  name: string;
}

export interface Authority {
  authority: string;
}

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  tel: string;
  address: string;
  password?: string;
  postId: string;
  role: Role;
  accountNonLocked: boolean;
  enabled: boolean;
  username: string;
  authorities: Authority[];
  accountNonExpired: boolean;
  credentialsNonExpired: boolean;
}

export interface Candidature {
  id: string;
  user: User;
  userId: number;
  postId: string;
  quizScore: number;
  totalQuizScore: number;
  atsScore: number;
  status: string;
  dateOfTest: string;
  startTime: string;
  endTime: string;
  interviewDateTime: string | null;
  durationSeconds: number;
  targetAchievement: number;
}