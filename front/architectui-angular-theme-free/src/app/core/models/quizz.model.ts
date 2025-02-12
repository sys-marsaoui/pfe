export type Quizz = {
    id:           string;
    title:        string;
    questions:    Question[];
    level:        string;
}

export type Question = {
    id:        string;
    text:      string;
    type:      null;
    options:   Option[];
    points:    number;
    time: number;
    technology: Technology;
}

export type Option = {
    id:      string;
    label:   null;
    correct: boolean;
}


export type Technology = {
    id:      string;
    name:   string;
    version: string;
}
