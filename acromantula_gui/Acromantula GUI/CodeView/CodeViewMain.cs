using System;
using System.Drawing;
using System.Windows.Forms;
using ScintillaNET;
using ScintillaNET_FindReplaceDialog;
using VPKSoft.ScintillaLexers;

namespace Acromantula_GUI.CodeView
{
    public partial class CodeViewMain : Form
    {

        private Scintilla scintilla { get; set; } = new Scintilla();

        private FindReplace Finder { get; set; } = new FindReplace();

        public CodeViewMain()
        {
            InitializeComponent();
        }

        private void Test_Load(object sender, EventArgs e)
        {
            editorContainer.Controls.Add(scintilla);

            scintilla.Dock = DockStyle.Fill;
            scintilla.Dock = DockStyle.Fill;

            scintilla.WrapMode = WrapMode.None;
            scintilla.IndentationGuides = IndentView.LookBoth;

            InitColors();
            InitSyntaxColoring();
            InitNumberMargin();
            InitBookmarkMargin();
            InitCodeFolding();
            InitDragDropFile();
            InitHotkeys();

            Finder.Scintilla = scintilla;
            Finder.KeyPressed += Finder_KeyPressed;
            Finder.FindAllResults += Finder_FindAllResults;
        }

        #region Finder
        private void Finder_KeyPressed(object sender, KeyEventArgs e)
        {
            OnHotkey(sender, e);
        }

        private void Finder_FindAllResults(object sender, FindResultsEventArgs FindAllResults)
        {
            searchResults.UpdateFindAllResults(FindAllResults.FindReplace, FindAllResults.FindAllResults);
        }
        #endregion

        #region Hotkey
        public void OnHotkey(object sender, KeyEventArgs e)
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
                GoTo MyGoTo = new GoTo((Scintilla)sender);
                MyGoTo.ShowGoToDialog();
                e.SuppressKeyPress = true;
            }
        }
        #endregion

        #region Init
        private void InitColors()
        {
            scintilla.SetSelectionBackColor(true, Color.DarkGray);
        }

        private void InitHotkeys()
        {
            scintilla.KeyDown += OnHotkey;

            // remove conflicting hotkeys from scintilla
            scintilla.ClearCmdKey(Keys.Control | Keys.F);
            scintilla.ClearCmdKey(Keys.Control | Keys.R);
            scintilla.ClearCmdKey(Keys.Control | Keys.H);
            scintilla.ClearCmdKey(Keys.Control | Keys.L);
            scintilla.ClearCmdKey(Keys.Control | Keys.U);
        }

        private void InitSyntaxColoring()
        {
            // Configure the default style
            scintilla.StyleResetDefault();
            scintilla.Styles[Style.Default].Font = "Consolas";
            scintilla.Styles[Style.Default].Size = 12;
            scintilla.Styles[Style.Default].BackColor = Color.White;
            scintilla.Styles[Style.Default].ForeColor = Color.Black;
            scintilla.StyleClearAll();

            ScintillaLexers.CreateLexer(scintilla, LexerEnumerations.LexerType.Java);

            scintilla.SetKeywords(0, "abstract assert boolean break byte case catch char class const continue default do double else enum extends final finally float for goto if implements import instanceof int interface long native new package private protected public return short static strictfp super switch synchronized this throw throws transient try void volatile while");
            scintilla.SetKeywords(1, "as fun in object is typeof typealias when val lateinit by var companion");
        }
        #endregion

        #region Numbers, Bookmarks, Code Folding

        /// <summary>
        /// the background color of the text area
        /// </summary>
        private const int BACK_COLOR = 0xEFEFEF;

        /// <summary>
        /// default text color of the text area
        /// </summary>
        private const int FORE_COLOR = 0x111111;

        /// <summary>
        /// change this to whatever margin you want the line numbers to show in
        /// </summary>
        private const int NUMBER_MARGIN = 1;

        /// <summary>
        /// change this to whatever margin you want the bookmarks/breakpoints to show in
        /// </summary>
        private const int BOOKMARK_MARGIN = 2;
        private const int BOOKMARK_MARKER = 2;

        /// <summary>
        /// change this to whatever margin you want the code folding tree (+/-) to show in
        /// </summary>
        private const int FOLDING_MARGIN = 3;

        /// <summary>
        /// set this true to show circular buttons for code folding (the [+] and [-] buttons on the margin)
        /// </summary>
        private const bool CODEFOLDING_CIRCULAR = false;

        private void InitNumberMargin()
        {

            scintilla.Styles[Style.LineNumber].BackColor = IntToColor(BACK_COLOR);
            scintilla.Styles[Style.LineNumber].ForeColor = IntToColor(FORE_COLOR);
            scintilla.Styles[Style.IndentGuide].ForeColor = IntToColor(FORE_COLOR);
            scintilla.Styles[Style.IndentGuide].BackColor = IntToColor(BACK_COLOR);

            var nums = scintilla.Margins[NUMBER_MARGIN];
            nums.Width = 30;
            nums.Type = MarginType.Number;
            nums.Sensitive = true;
            nums.Mask = 0;

            scintilla.MarginClick += scintilla_MarginClick;
        }

        private void InitBookmarkMargin()
        {

            //scintilla.SetFoldMarginColor(true, IntToColor(BACK_COLOR));

            var margin = scintilla.Margins[BOOKMARK_MARGIN];
            margin.Width = 20;
            margin.Sensitive = true;
            margin.Type = MarginType.Symbol;
            margin.Mask = (1 << BOOKMARK_MARKER);
            //margin.Cursor = MarginCursor.Arrow;

            var marker = scintilla.Markers[BOOKMARK_MARKER];
            marker.Symbol = MarkerSymbol.Circle;
            marker.SetBackColor(IntToColor(0xFF003B));
            marker.SetForeColor(IntToColor(0x000000));
            marker.SetAlpha(100);

        }

        private void InitCodeFolding()
        {

            scintilla.SetFoldMarginColor(true, IntToColor(BACK_COLOR));
            scintilla.SetFoldMarginHighlightColor(true, IntToColor(BACK_COLOR));

            // Enable code folding
            scintilla.SetProperty("fold", "1");
            scintilla.SetProperty("fold.compact", "1");

            // Configure a margin to display folding symbols
            scintilla.Margins[FOLDING_MARGIN].Type = MarginType.Symbol;
            scintilla.Margins[FOLDING_MARGIN].Mask = Marker.MaskFolders;
            scintilla.Margins[FOLDING_MARGIN].Sensitive = true;
            scintilla.Margins[FOLDING_MARGIN].Width = 20;

            // Set colors for all folding markers
            for (int i = 25; i <= 31; i++)
            {
                scintilla.Markers[i].SetForeColor(IntToColor(BACK_COLOR)); // styles for [+] and [-]
                scintilla.Markers[i].SetBackColor(IntToColor(FORE_COLOR)); // styles for [+] and [-]
            }

            // Configure folding markers with respective symbols
            scintilla.Markers[Marker.Folder].Symbol = CODEFOLDING_CIRCULAR ? MarkerSymbol.CirclePlus : MarkerSymbol.BoxPlus;
            scintilla.Markers[Marker.FolderOpen].Symbol = CODEFOLDING_CIRCULAR ? MarkerSymbol.CircleMinus : MarkerSymbol.BoxMinus;
            scintilla.Markers[Marker.FolderEnd].Symbol = CODEFOLDING_CIRCULAR ? MarkerSymbol.CirclePlusConnected : MarkerSymbol.BoxPlusConnected;
            scintilla.Markers[Marker.FolderMidTail].Symbol = MarkerSymbol.TCorner;
            scintilla.Markers[Marker.FolderOpenMid].Symbol = CODEFOLDING_CIRCULAR ? MarkerSymbol.CircleMinusConnected : MarkerSymbol.BoxMinusConnected;
            scintilla.Markers[Marker.FolderSub].Symbol = MarkerSymbol.VLine;
            scintilla.Markers[Marker.FolderTail].Symbol = MarkerSymbol.LCorner;

            // Enable automatic folding
            scintilla.AutomaticFold = (AutomaticFold.Show | AutomaticFold.Click | AutomaticFold.Change);
        }

        private void scintilla_MarginClick(object sender, MarginClickEventArgs e)
        {
            if (e.Margin == BOOKMARK_MARGIN)
            {
                // Do we have a marker for this line?
                const uint mask = (1 << BOOKMARK_MARKER);
                var line = scintilla.Lines[scintilla.LineFromPosition(e.Position)];
                if ((line.MarkerGet() & mask) > 0)
                {
                    // Remove existing bookmark
                    line.MarkerDelete(BOOKMARK_MARKER);
                }
                else
                {
                    // Add bookmark
                    line.MarkerAdd(BOOKMARK_MARKER);
                }
            }
        }

        #endregion

        #region Drag & Drop File

        public void InitDragDropFile()
        {

            scintilla.AllowDrop = true;
            scintilla.DragEnter += delegate (object sender, DragEventArgs e) {
                if (e.Data.GetDataPresent(DataFormats.FileDrop))
                    e.Effect = DragDropEffects.Copy;
                else
                    e.Effect = DragDropEffects.None;
            };
            scintilla.DragDrop += delegate (object sender, DragEventArgs e) {

                // get file drop
                if (e.Data.GetDataPresent(DataFormats.FileDrop))
                {

                    Array a = (Array)e.Data.GetData(DataFormats.FileDrop);
                    if (a != null)
                    {

                        string path = a.GetValue(0).ToString();

                        //LoadDataFromFile(path);

                    }
                }
            };
        }

        #endregion

        #region Main Menu Commands

        private void cutToolStripMenuItem_Click(object sender, EventArgs e)
        {
            scintilla.Cut();
        }

        private void copyToolStripMenuItem_Click(object sender, EventArgs e)
        {
            scintilla.Copy();
        }

        private void pasteToolStripMenuItem_Click(object sender, EventArgs e)
        {
            scintilla.Paste();
        }

        private void selectAllToolStripMenuItem_Click(object sender, EventArgs e)
        {
            scintilla.SelectAll();
        }

        private void selectLineToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Line line = scintilla.Lines[scintilla.CurrentLine];
            scintilla.SetSelection(line.Position + line.Length, line.Position);
        }

        private void clearSelectionToolStripMenuItem_Click(object sender, EventArgs e)
        {
            scintilla.SetEmptySelection(0);
        }

        private void indentSelectionToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Indent();
        }

        private void outdentSelectionToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Outdent();
        }

        private void uppercaseSelectionToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Uppercase();
        }

        private void lowercaseSelectionToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Lowercase();
        }

        private void zoomInToolStripMenuItem_Click(object sender, EventArgs e)
        {
            ZoomIn();
        }

        private void zoomOutToolStripMenuItem_Click(object sender, EventArgs e)
        {
            ZoomOut();
        }

        private void zoom100ToolStripMenuItem_Click(object sender, EventArgs e)
        {
            ZoomDefault();
        }

        private void collapseAllToolStripMenuItem_Click(object sender, EventArgs e)
        {
            scintilla.FoldAll(FoldAction.Contract);
        }

        private void expandAllToolStripMenuItem_Click(object sender, EventArgs e)
        {
            scintilla.FoldAll(FoldAction.Expand);
        }
        #endregion

        #region Uppercase / Lowercase

        private void Lowercase()
        {

            // save the selection
            int start = scintilla.SelectionStart;
            int end = scintilla.SelectionEnd;

            // modify the selected text
            scintilla.ReplaceSelection(scintilla.GetTextRange(start, end - start).ToLower());

            // preserve the original selection
            scintilla.SetSelection(start, end);
        }

        private void Uppercase()
        {

            // save the selection
            int start = scintilla.SelectionStart;
            int end = scintilla.SelectionEnd;

            // modify the selected text
            scintilla.ReplaceSelection(scintilla.GetTextRange(start, end - start).ToUpper());

            // preserve the original selection
            scintilla.SetSelection(start, end);
        }

        #endregion

        #region Indent / Outdent

        private void Indent()
        {
            // we use this hack to send "Shift+Tab" to scintilla, since there is no known API to indent,
            // although the indentation function exists. Pressing TAB with the editor focused confirms this.
            GenerateKeystrokes("{TAB}");
        }

        private void Outdent()
        {
            // we use this hack to send "Shift+Tab" to scintilla, since there is no known API to outdent,
            // although the indentation function exists. Pressing Shift+Tab with the editor focused confirms this.
            GenerateKeystrokes("+{TAB}");
        }

        private void GenerateKeystrokes(string keys)
        {
            scintilla.Focus();
            SendKeys.Send(keys);
        }

        #endregion

        #region Zoom

        private void ZoomIn()
        {
            scintilla.ZoomIn();
        }

        private void ZoomOut()
        {
            scintilla.ZoomOut();
        }

        private void ZoomDefault()
        {
            scintilla.Zoom = 0;
        }


        #endregion

        #region Utils

        public static Color IntToColor(int rgb)
        {
            return Color.FromArgb(255, (byte)(rgb >> 16), (byte)(rgb >> 8), (byte)rgb);
        }

        #endregion
    }
}
