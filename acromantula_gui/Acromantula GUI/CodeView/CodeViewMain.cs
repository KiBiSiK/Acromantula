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
        #region Props

        private Scintilla Scintilla { get; } = new Scintilla();
        private FindReplace Finder { get; } = new FindReplace();

        #endregion

        #region Constructor

        public CodeViewMain()
        {
            InitializeComponent();
        }

        #endregion

        #region Load

        private void CodeViewMain_Load(object sender, EventArgs e)
        {
            editorContainer.Controls.Add(Scintilla);

            Scintilla.Dock = DockStyle.Fill;
            Scintilla.Dock = DockStyle.Fill;

            Scintilla.WrapMode = WrapMode.None;
            Scintilla.IndentationGuides = IndentView.LookBoth;

            InitColors();
            InitSyntaxColoring();
            InitNumberMargin();
            InitBookmarkMargin();
            InitCodeFolding();
            InitHotkeys();

            Finder.Scintilla = Scintilla;
            Finder.KeyPressed += Finder_KeyPressed;
            Finder.FindAllResults += Finder_FindAllResults;
        }

        #endregion

        #region Finder

        private void Finder_KeyPressed(object sender, KeyEventArgs e)
        {
            OnHotkey(sender, e);
        }

        private void Finder_FindAllResults(object sender, FindResultsEventArgs findAllResults)
        {
            searchResults.UpdateFindAllResults(findAllResults.FindReplace, findAllResults.FindAllResults);
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
                new GoTo((Scintilla) sender).ShowGoToDialog();
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

            // remove conflicting hotkeys from scintilla
            Scintilla.ClearCmdKey(Keys.Control | Keys.F);
            Scintilla.ClearCmdKey(Keys.Control | Keys.R);
            Scintilla.ClearCmdKey(Keys.Control | Keys.H);
            Scintilla.ClearCmdKey(Keys.Control | Keys.L);
            Scintilla.ClearCmdKey(Keys.Control | Keys.U);
        }

        private void InitSyntaxColoring()
        {
            // Configure the default style
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

        #region Numbers, Bookmarks, Code Folding

        private const int ColorBack = 0xEFEFEF;
        private const int ColorFore = 0x111111;
        private const int NumberMargin = 1;
        private const int BookmarkMargin = 2;
        private const int BookmarkMarker = 2;
        private const int FoldingMargin = 3;

        private const bool CodefoldingCircular = false;

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
            //scintilla.SetFoldMarginColor(true, IntToColor(BACK_COLOR));

            var margin = Scintilla.Margins[BookmarkMargin];
            margin.Width = 20;
            margin.Sensitive = true;
            margin.Type = MarginType.Symbol;
            margin.Mask = (1 << BookmarkMarker);
            //margin.Cursor = MarginCursor.Arrow;

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

            // Enable code folding
            Scintilla.SetProperty("fold", "1");
            Scintilla.SetProperty("fold.compact", "1");

            // Configure a margin to display folding symbols
            Scintilla.Margins[FoldingMargin].Type = MarginType.Symbol;
            Scintilla.Margins[FoldingMargin].Mask = Marker.MaskFolders;
            Scintilla.Margins[FoldingMargin].Sensitive = true;
            Scintilla.Margins[FoldingMargin].Width = 20;

            // Set colors for all folding markers
            for (var i = 25; i <= 31; i++)
            {
                Scintilla.Markers[i].SetForeColor(IntToColor(ColorBack)); // styles for [+] and [-]
                Scintilla.Markers[i].SetBackColor(IntToColor(ColorFore)); // styles for [+] and [-]
            }

            // Configure folding markers with respective symbols
            Scintilla.Markers[Marker.Folder].Symbol =
                CodefoldingCircular ? MarkerSymbol.CirclePlus : MarkerSymbol.BoxPlus;
            Scintilla.Markers[Marker.FolderOpen].Symbol =
                CodefoldingCircular ? MarkerSymbol.CircleMinus : MarkerSymbol.BoxMinus;
            Scintilla.Markers[Marker.FolderEnd].Symbol =
                CodefoldingCircular ? MarkerSymbol.CirclePlusConnected : MarkerSymbol.BoxPlusConnected;
            Scintilla.Markers[Marker.FolderMidTail].Symbol = MarkerSymbol.TCorner;
            Scintilla.Markers[Marker.FolderOpenMid].Symbol = CodefoldingCircular
                ? MarkerSymbol.CircleMinusConnected
                : MarkerSymbol.BoxMinusConnected;
            Scintilla.Markers[Marker.FolderSub].Symbol = MarkerSymbol.VLine;
            Scintilla.Markers[Marker.FolderTail].Symbol = MarkerSymbol.LCorner;

            // Enable automatic folding
            Scintilla.AutomaticFold = (AutomaticFold.Show | AutomaticFold.Click | AutomaticFold.Change);
        }

        private void scintilla_MarginClick(object sender, MarginClickEventArgs e)
        {
            if (e.Margin != BookmarkMargin) return;

            // Do we have a marker for this line?
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

        private static Color IntToColor(int rgb)
        {
            return Color.FromArgb(255, (byte) (rgb >> 16), (byte) (rgb >> 8), (byte) rgb);
        }

        #endregion
    }
}