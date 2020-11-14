using System.Collections.Generic;
using System.Drawing;
using System.Text.RegularExpressions;
using System.Windows.Forms;
using ScintillaNET;
using ScintillaNET_FindReplaceDialog;
using VPKSoft.ScintillaLexers;

namespace Acromantula_GUI.CodeView
{
    public class CodeViewMain
    {
        #region Props

        public Scintilla Scintilla { get; } = new Scintilla();
        private FindReplace Finder { get; } = new FindReplace();

        #endregion

        #region Constructor
        public CodeViewMain()
        {
            Scintilla.Dock = DockStyle.Fill;

            Scintilla.MultipleSelection = true;
            Scintilla.MouseSelectionRectangularSwitch = true;
            Scintilla.AdditionalSelectionTyping = true;
            Scintilla.VirtualSpaceOptions = VirtualSpace.RectangularSelection;

            Scintilla.WrapMode = WrapMode.None;
            Scintilla.IndentationGuides = IndentView.LookBoth;

            Scintilla.CharAdded += Scintilla_CharAdded;
            Scintilla.InsertCheck += Scintilla_InsertCheck;
            Scintilla.UpdateUI += Scintilla_UpdateUI;

            InitColors();
            InitSyntaxColoring();
            InitNumberMargin();
            InitBookmarkMargin();
            InitCodeFolding();
            InitHotkeys();

            Finder.Scintilla = Scintilla;
            Finder.KeyPressed += Finder_KeyPressed;
        }
        #endregion

        #region Finder

        private void Finder_KeyPressed(object sender, KeyEventArgs e)
        {
            OnHotkey(sender, e);
        }

        #endregion

        #region Hotkey

        private void OnHotkey(object sender, KeyEventArgs e)
        {
            if (e.Control && e.KeyCode == Keys.F)
            {
                Finder.ShowFind();
                e.SuppressKeyPress = true;
            }
            else if (e.Shift && e.KeyCode == Keys.F3)
            {
                Finder.Window.FindPrevious();
                e.SuppressKeyPress = true;
            }
            else if (e.KeyCode == Keys.F3)
            {
                Finder.Window.FindNext();
                e.SuppressKeyPress = true;
            }
            else if (e.Control && e.KeyCode == Keys.H)
            {
                Finder.ShowReplace();
                e.SuppressKeyPress = true;
            }
            else if (e.Control && e.KeyCode == Keys.I)
            {
                Finder.ShowIncrementalSearch();
                e.SuppressKeyPress = true;
            }
            else if (e.Control && e.KeyCode == Keys.G)
            {
                new GoTo((Scintilla)sender).ShowGoToDialog();
                e.SuppressKeyPress = true;
            }
        }

        #endregion

        #region Init
        private void InitColors()
        {
            Scintilla.SetSelectionBackColor(true, Color.DarkGray);
        }

        private void InitHotkeys()
        {
            Scintilla.KeyDown += OnHotkey;

            Scintilla.ClearCmdKey(Keys.Control | Keys.F);
            Scintilla.ClearCmdKey(Keys.Control | Keys.R);
            Scintilla.ClearCmdKey(Keys.Control | Keys.H);
            Scintilla.ClearCmdKey(Keys.Control | Keys.L);
            Scintilla.ClearCmdKey(Keys.Control | Keys.U);
        }

        private void InitSyntaxColoring()
        {
            Scintilla.StyleResetDefault();
            Scintilla.Styles[Style.Default].Font = "Consolas";
            Scintilla.Styles[Style.Default].Size = 12;
            Scintilla.Styles[Style.Default].BackColor = Color.White;
            Scintilla.Styles[Style.Default].ForeColor = Color.Black;
            Scintilla.StyleClearAll();

            ScintillaLexers.CreateLexer(Scintilla, LexerEnumerations.LexerType.Java);

            Scintilla.SetKeywords(0,
                "abstract assert boolean break byte case catch char class const continue default do double else enum extends final finally float for goto if implements import instanceof int interface long native new package private protected public return short static strictfp super switch synchronized this throw throws transient try void volatile while");
            Scintilla.SetKeywords(1, "as fun in object is typeof typealias when val lateinit by var companion");
        }

        #endregion

        #region CodeIndent

        private void Scintilla_InsertCheck(object sender, InsertCheckEventArgs e)
        {
            if (e.Text.Length == 1)
            {
                var caretPos = Scintilla.CurrentPosition;
                var charNext = Scintilla.GetCharAt(caretPos);

                if (charNext == ')' && e.Text.ToLower() == ")" || charNext == '}' && e.Text.ToLower() == "}" ||
                    charNext == ']' && e.Text.ToLower() == "]")
                {
                    var shouldSkip = charNext == ')' ||
                                     charNext == '}' ||
                                     charNext == ']';

                    if (shouldSkip)
                    {
                        e.Text = "";
                        Scintilla.GotoPosition(Scintilla.CurrentPosition + 1);
                    }
                }
            }

            if (!e.Text.EndsWith("\r") && !e.Text.EndsWith("\n")) return;

            var line = Scintilla.Lines[Scintilla.LineFromPosition(Scintilla.CurrentPosition)];
            var startPos = Scintilla.Lines[Scintilla.LineFromPosition(Scintilla.CurrentPosition)].Position;
            var endPos = e.Position;

            var curLineText = Scintilla.GetTextRange(startPos, endPos - startPos);

            var indent = Regex.Match(curLineText, "^[ \\t]*");

            e.Text += indent.Value;

            if (Regex.IsMatch(curLineText, "{\\s*$") && !line.Text.Trim().EndsWith("}"))
            {
                e.Text += "\t";
            }
        }

        #endregion

        #region Highlighting

        private int _lastCaretPos;

        private void Scintilla_UpdateUI(object sender, UpdateUIEventArgs e)
        {
            var caretPos = Scintilla.CurrentPosition;

            if (_lastCaretPos == caretPos) return;

            _lastCaretPos = caretPos;

            var openingBracePos = -1;

            if (caretPos > 0 && IsBrace(Scintilla.GetCharAt(caretPos - 1)))
            {
                openingBracePos = caretPos - 1;
            }
            else if (IsBrace(Scintilla.GetCharAt(caretPos)))
            {
                openingBracePos = caretPos;
            }

            if (openingBracePos != -1)
            {
                var closingBracePos = Scintilla.BraceMatch(openingBracePos);

                if (closingBracePos == Scintilla.InvalidPosition)
                {
                    Scintilla.BraceBadLight(openingBracePos);
                    Scintilla.HighlightGuide = 0;
                }
                else
                {
                    Scintilla.BraceHighlight(openingBracePos, closingBracePos);
                    Scintilla.HighlightGuide = Scintilla.GetColumn(openingBracePos);
                }
            }
            else
            {
                Scintilla.BraceHighlight(Scintilla.InvalidPosition, Scintilla.InvalidPosition);
                Scintilla.HighlightGuide = 0;
            }
        }

        #endregion

        #region Autocompletion

        private void Scintilla_CharAdded(object sender, CharAddedEventArgs e)
        {
            var currentLineIndex = Scintilla.LineFromPosition(Scintilla.CurrentPosition);
            var currentLine = Scintilla.Lines[currentLineIndex];

            switch (e.Char)
            {
                case '.':
                    {
                        var list = new List<string> { "out", "in", "println", "currentTimeMillis" };
                        list.Sort();
                        Scintilla.AutoCShow(
                            Scintilla.CurrentPosition - Scintilla.WordStartPosition(Scintilla.CurrentPosition, true),
                            string.Join(" ", list));
                        break;
                    }
                case '}':
                    if (Scintilla.Lines[currentLineIndex].Text.Trim() == "}")
                    {
                        currentLine.Indentation -= Scintilla.TabWidth;
                    }

                    break;
                case '\n':
                    var openingBraceLine = Scintilla.Lines[currentLineIndex - 2];
                    var prevLine = Scintilla.Lines[currentLineIndex - 1];

                    if (Regex.IsMatch(openingBraceLine.Text, "{\\s*$"))
                    {
                        Scintilla.InsertText(prevLine.EndPosition, "\n");
                        currentLine.Indentation = prevLine.Indentation + Scintilla.TabWidth;
                        Scintilla.GotoPosition(currentLine.Position + currentLine.Indentation);
                    }

                    break;
                default:
                    InsertMatchedChars(e);
                    break;
            }
        }

        private void InsertMatchedChars(CharAddedEventArgs e)
        {
            var caretPos = Scintilla.CurrentPosition;
            var docStart = caretPos == 1;
            var docEnd = caretPos == Scintilla.Text.Length;

            var charPrev = docStart ? Scintilla.GetCharAt(caretPos) : Scintilla.GetCharAt(caretPos - 2);
            var charNext = Scintilla.GetCharAt(caretPos);

            var isCharPrevBlank = charPrev == ' ' || charPrev == '\t' ||
                                  charPrev == '\n' || charPrev == '\r';

            var isCharNextBlank = charNext == ' ' || charNext == '\t' ||
                                  charNext == '\n' || charNext == '\r' ||
                                  docEnd;

            var isEnclosed = charPrev == '(' && charNext == ')' ||
                             charPrev == '{' && charNext == '}' ||
                             charPrev == '[' && charNext == ']';

            var isSpaceEnclosed = charPrev == '(' && isCharNextBlank || isCharPrevBlank && charNext == ')' ||
                                  charPrev == '{' && isCharNextBlank || isCharPrevBlank && charNext == '}' ||
                                  charPrev == '[' && isCharNextBlank || isCharPrevBlank && charNext == ']';

            var isCharOrString = isCharPrevBlank && isCharNextBlank || isEnclosed || isSpaceEnclosed;

            var charNextIsCharOrString = charNext == '"' || charNext == '\'';

            switch (e.Char)
            {
                case '(':
                    if (charNextIsCharOrString)
                    {
                        return;
                    }

                    Scintilla.InsertText(caretPos, ")");

                    break;
                case '{':
                    if (charNextIsCharOrString)
                    {
                        return;
                    }

                    if (isEnclosed)
                    {
                        Scintilla.GotoPosition(caretPos + 1);
                    }
                    else
                    {
                        Scintilla.InsertText(caretPos, "}");
                    }

                    break;
                case '[':
                    if (charNextIsCharOrString)
                    {
                        return;
                    }

                    if (isEnclosed)
                    {
                        Scintilla.GotoPosition(caretPos + 1);
                    }
                    else
                    {
                        Scintilla.InsertText(caretPos, "]");
                    }

                    break;
                case '"':
                    if (charPrev == '\"' && charNext == '\"')
                    {
                        Scintilla.DeleteRange(caretPos, 1);
                        Scintilla.GotoPosition(caretPos);
                        return;
                    }

                    if (isCharOrString)
                    {
                        Scintilla.InsertText(caretPos, "\"");
                    }

                    break;
                case '\'':
                    if (charPrev == '\'' && charNext == '\'')
                    {
                        Scintilla.DeleteRange(caretPos, 1);
                        Scintilla.GotoPosition(caretPos);

                        return;
                    }

                    if (isCharOrString)
                    {
                        Scintilla.InsertText(caretPos, "'");
                    }

                    break;
            }
        }

        #endregion

        #region Numbers, Bookmarks, Code Folding

        private const int ColorBack = 0xEFEFEF;
        private const int ColorFore = 0x111111;

        private const int NumberMargin = 1;
        private const int BookmarkMargin = 2;
        private const int BookmarkMarker = 2;
        private const int FoldingMargin = 3;


        private void InitNumberMargin()
        {
            Scintilla.Styles[Style.LineNumber].BackColor = IntToColor(ColorBack);
            Scintilla.Styles[Style.LineNumber].ForeColor = IntToColor(ColorFore);
            Scintilla.Styles[Style.IndentGuide].ForeColor = IntToColor(ColorFore);
            Scintilla.Styles[Style.IndentGuide].BackColor = IntToColor(ColorBack);

            var nums = Scintilla.Margins[NumberMargin];
            nums.Width = 30;
            nums.Type = MarginType.Number;
            nums.Sensitive = true;
            nums.Mask = 0;

            Scintilla.MarginClick += scintilla_MarginClick;
        }

        private void InitBookmarkMargin()
        {
            var margin = Scintilla.Margins[BookmarkMargin];
            margin.Width = 20;
            margin.Sensitive = true;
            margin.Type = MarginType.Symbol;
            margin.Mask = 1 << BookmarkMarker;
            margin.Cursor = MarginCursor.Arrow;

            var marker = Scintilla.Markers[BookmarkMarker];
            marker.Symbol = MarkerSymbol.Circle;
            marker.SetBackColor(IntToColor(0xFF003B));
            marker.SetForeColor(IntToColor(0x000000));
            marker.SetAlpha(100);
        }

        private void InitCodeFolding()
        {
            Scintilla.SetFoldMarginColor(true, IntToColor(ColorBack));
            Scintilla.SetFoldMarginHighlightColor(true, IntToColor(ColorBack));

            Scintilla.SetProperty("fold", "1");
            Scintilla.SetProperty("fold.compact", "1");

            Scintilla.Margins[FoldingMargin].Type = MarginType.Symbol;
            Scintilla.Margins[FoldingMargin].Mask = Marker.MaskFolders;
            Scintilla.Margins[FoldingMargin].Sensitive = true;
            Scintilla.Margins[FoldingMargin].Width = 20;

            for (var i = 25; i <= 31; i++)
            {
                Scintilla.Markers[i].SetForeColor(IntToColor(ColorBack));
                Scintilla.Markers[i].SetBackColor(IntToColor(ColorFore));
            }

            Scintilla.Markers[Marker.Folder].Symbol = MarkerSymbol.BoxPlus;
            Scintilla.Markers[Marker.FolderOpen].Symbol = MarkerSymbol.BoxMinus;
            Scintilla.Markers[Marker.FolderEnd].Symbol = MarkerSymbol.BoxPlusConnected;
            Scintilla.Markers[Marker.FolderMidTail].Symbol = MarkerSymbol.TCorner;
            Scintilla.Markers[Marker.FolderOpenMid].Symbol = MarkerSymbol.BoxMinusConnected;
            Scintilla.Markers[Marker.FolderSub].Symbol = MarkerSymbol.VLine;
            Scintilla.Markers[Marker.FolderTail].Symbol = MarkerSymbol.LCorner;

            Scintilla.AutomaticFold = AutomaticFold.Show | AutomaticFold.Click | AutomaticFold.Change;
        }

        private void scintilla_MarginClick(object sender, MarginClickEventArgs e)
        {
            if (e.Margin != BookmarkMargin)
            {
                return;
            }

            const uint mask = (1 << BookmarkMarker);

            var line = Scintilla.Lines[Scintilla.LineFromPosition(e.Position)];

            if ((line.MarkerGet() & mask) > 0)
            {
                line.MarkerDelete(BookmarkMarker);
            }
            else
            {
                line.MarkerAdd(BookmarkMarker);
            }
        }

        #endregion

        #region Utils

        private static bool IsBrace(int c)
        {
            return c == '[' || c == ']' || c == '{' || c == '}' || c == '(' || c == ')';
        }

        private static Color IntToColor(int rgb)
        {
            return Color.FromArgb(255, (byte)(rgb >> 16), (byte)(rgb >> 8), (byte)rgb);
        }

        #endregion
    }
}